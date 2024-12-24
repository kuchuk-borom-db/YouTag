import React, {type ChangeEvent, type KeyboardEvent, useCallback, useEffect, useRef, useState} from 'react';
import {AlertTriangle, Check, Tag, X, Youtube} from 'lucide-react';
import {addTagsToVideo, getAllTags, getTagsContainingKeyword} from "../../pages/api/TagService.ts";

interface TagYoutubeModalProps {
    initialTags?: string[];
    initialVideos?: string[];
    onClose: () => void;
}

const extractYouTubeVideoId = (url: string): string | null => {
    // More comprehensive regular expressions to match YouTube URL formats
    const patterns = [
        // Standard YouTube URLs
        /(?:https?:\/\/)?(?:www\.)?youtube\.com\/watch\?v=([a-zA-Z0-9_-]{11})/,
        // Shortened youtu.be URLs
        /(?:https?:\/\/)?(?:www\.)?youtu\.be\/([a-zA-Z0-9_-]{11})/,
        // Embedded YouTube URLs
        /(?:https?:\/\/)?(?:www\.)?youtube\.com\/embed\/([a-zA-Z0-9_-]{11})/,
        // Direct video ID
        /^([a-zA-Z0-9_-]{11})$/
    ];

    for (const pattern of patterns) {
        const match = url.match(pattern);
        if (match && match[1]) {
            return match[1];
        }
    }
    return null;
};

const TagYoutubeModal: React.FC<TagYoutubeModalProps> = ({
                                                             initialTags = [],
                                                             initialVideos = [],
                                                             onClose
                                                         }) => {
    // Initialize state with initial props, filtering out any invalid items
    const [tags, setTags] = useState<string[]>(
        initialTags.filter(tag => tag && tag.trim() !== '')
    );
    const [inputValue, setInputValue] = useState('');

    // Filter initial videos to ensure they are valid YouTube links
    const [youtubeLinks, setYoutubeLinks] = useState<string[]>(
        initialVideos
            .map(video => {
                const videoId = extractYouTubeVideoId(video);
                return videoId ? video : null;
            })
            .filter((video): video is string => video !== null)
    );

    const [youtubeInput, setYoutubeInput] = useState('');
    const [suggestions, setSuggestions] = useState<string[]>([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [suggestionsPerPage] = useState(10);
    const [totalTagCount, setTotalTagCount] = useState(0);
    const [submitStatus, setSubmitStatus] = useState<{
        status: 'idle' | 'loading' | 'success' | 'error',
        message?: string
    }>({status: 'idle'});
    const inputRef = useRef<HTMLInputElement>(null);
    const debounceTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    // Fetch tags with debounce and keyword support
    const fetchTagSuggestions = useCallback(async (keyword: string = '', page: number = 1) => {
        try {
            const skip = (page - 1) * suggestionsPerPage;

            let tagResults: string[];
            let totalCount: number;

            if (keyword.trim() === '') {
                // If no keyword, fetch all tags
                const result = await getAllTags(skip, suggestionsPerPage);
                tagResults = result?.tags || [];
                totalCount = result?.count || 0;
            } else {
                // If keyword exists, use getTagsContainingKeyword
                const result = await getTagsContainingKeyword(keyword, skip, suggestionsPerPage);
                tagResults = result?.tags || [];
                totalCount = result?.count || 0;
            }

            // Filter out tags that are already selected
            const filteredTags = tagResults.filter(tag => !tags.includes(tag));

            setSuggestions(filteredTags);
            setTotalTagCount(totalCount);
        } catch (error) {
            console.error("Failed to fetch tags:", error);
            setSuggestions([]);
            setTotalTagCount(0);
        }
    }, [tags, suggestionsPerPage]);

    // Pagination handler
    const handlePageChange = (pageNumber: number) => {
        setCurrentPage(pageNumber);
    };

    // Debounced search effect
    useEffect(() => {
        // Clear previous timeout
        if (debounceTimeoutRef.current) {
            clearTimeout(debounceTimeoutRef.current);
        }

        // Set new timeout
        debounceTimeoutRef.current = setTimeout(() => {
            fetchTagSuggestions(inputValue, currentPage);
        }, 1000); // 1000ms delay

        // Cleanup timeout on unmount or dependency change
        return () => {
            if (debounceTimeoutRef.current) {
                clearTimeout(debounceTimeoutRef.current);
            }
        };
    }, [inputValue, currentPage, fetchTagSuggestions]);

    const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;

        // Check if last character is a comma to add tag
        if (value.endsWith(',')) {
            const newTag = value.slice(0, -1).trim().toLowerCase();
            const isValidTag = /^[a-z0-9_]+$/.test(newTag); // Ensures only alphanumeric and underscore, no spaces or special symbols
            if (newTag && isValidTag && !tags.includes(newTag)) {
                setTags([...tags, newTag]);
                setInputValue('');
                setCurrentPage(1);
            }
        } else {
            // Prevent spaces and special characters (except '_') while typing
            const sanitizedValue = value.replace(/[^a-z0-9_]/gi, '');
            setInputValue(sanitizedValue);
        }
    };


    const handleYoutubeInputChange = (e: ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;

        // Check if last character is a comma to add YouTube link
        if (value.endsWith(',')) {
            const newLink = value.slice(0, -1).trim();
            const videoId = extractYouTubeVideoId(newLink);
            if (videoId && !youtubeLinks.includes(newLink)) {
                setYoutubeLinks([...youtubeLinks, newLink]);
                setYoutubeInput('');
            }
        } else {
            setYoutubeInput(value);
        }
    };

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        // Handle backspace to remove tags
        if (e.key === 'Backspace' && inputValue === '' && tags.length > 0) {
            setTags(tags.slice(0, -1));
        }
    };

    const removeTag = (tagToRemove: string) => {
        setTags(tags.filter(tag => tag !== tagToRemove));
    };

    const addSuggestedTag = (suggestedTag: string) => {
        if (!tags.includes(suggestedTag)) {
            setTags([...tags, suggestedTag]);
            setInputValue('');
            setCurrentPage(1);
        }
    };

    const removeYoutubeLink = (linkToRemove: string) => {
        setYoutubeLinks(youtubeLinks.filter(link => link !== linkToRemove));
    };

    const handleSubmit = async () => {
        if (tags.length === 0 || youtubeLinks.length === 0) {
            setSubmitStatus({
                status: 'error',
                message: 'Please add at least one tag and one YouTube link'
            });
            return;
        }

        try {
            setSubmitStatus({status: 'loading', message: 'Adding videos...'});

            // Extract video IDs
            const videoIds = youtubeLinks
                .map(link => extractYouTubeVideoId(link))
                .filter((id): id is string => id !== null);

            // Add tags to videos
            const result = await addTagsToVideo(videoIds, tags);

            if (result) {
                setSubmitStatus({status: 'success', message: 'Videos successfully tagged!'});

                setTags([]);
                if (initialVideos && initialVideos.length > 0) {
                    setYoutubeLinks(initialVideos);
                } else {
                    setYoutubeLinks([]);
                }
                setInputValue('');
                setYoutubeInput('');

            } else {
                setSubmitStatus({
                    status: 'error',
                    message: 'Failed to add tags to videos'
                });
            }
        } catch (error) {
            setSubmitStatus({
                status: 'error',
                message: 'An error occurred while adding videos'
            });
            console.error(error);
        }
    };

    const totalPages = Math.ceil(totalTagCount / suggestionsPerPage);

    return (
        <div
            className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 animate-fade-in"
            onClick={onClose}
        >
            <div
                className="bg-white rounded-xl p-6 w-[500px] max-w-[90%] shadow-2xl animate-slide-up"
                onClick={(e) => e.stopPropagation()}
            >
                {/* Close Button */}
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 text-gray-500 hover:text-gray-700"
                >
                    <X className="w-6 h-6"/>
                </button>

                {/* Tags Input Section */}
                <div className="mb-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2 items-center">
                        <Tag className="mr-2 w-5 h-5 text-blue-500"/>
                        Tags
                    </label>

                    {/* Tags Container */}
                    <div className="border rounded-lg p-2 flex flex-wrap gap-2 mb-2 min-h-[50px]">
                        {tags.map((tag) => (
                            <span
                                key={tag}
                                className="bg-blue-100 text-blue-700 px-2 py-1 rounded-full flex items-center text-sm"
                            >
                                {tag}
                                <button
                                    onClick={() => removeTag(tag)}
                                    className="ml-2 hover:text-red-500"
                                >
                                    <X className="w-4 h-4"/>
                                </button>
                            </span>
                        ))}

                        {/* Input Field */}
                        <input
                            ref={inputRef}
                            value={inputValue}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyDown}
                            placeholder="Add tags, separate by comma"
                            className="flex-grow outline-none px-1 text-sm text-black"
                        />
                    </div>

                    {/* Suggestions */}
                    {suggestions.length > 0 && (
                        <div>
                            <p className="text-xs text-gray-500 mb-2">Suggestions:</p>
                            <div className="flex flex-wrap gap-2">
                                {suggestions.map((suggestion) => (
                                    <button
                                        key={suggestion}
                                        onClick={() => addSuggestedTag(suggestion)}
                                        className="bg-gray-100 text-gray-700 px-2 py-1 rounded-full text-xs hover:bg-amber-800 transition-colors"
                                    >
                                        {suggestion}
                                    </button>
                                ))}
                            </div>

                            {/* Pagination */}
                            <div className="flex justify-center mt-4">
                                {Array.from({length: totalPages}, (_, index) => index + 1).map((pageNumber) => (
                                    <button
                                        key={pageNumber}
                                        onClick={() => handlePageChange(pageNumber)}
                                        className={`px-3 py-1 rounded-md mx-1 ${
                                            currentPage === pageNumber
                                                ? 'bg-black text-white'
                                                : 'bg-gray-100 text-gray-700 hover:bg-blue-100 transition-colors'
                                        }`}
                                    >
                                        {pageNumber}
                                    </button>
                                ))}
                            </div>
                        </div>
                    )}
                </div>

                {/* YouTube Links Input */}
                <div className="mb-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2 items-center">
                        <Youtube className="mr-2 w-5 h-5 text-red-500"/>
                        YouTube Links
                    </label>

                    {/* YouTube Links Container */}
                    {youtubeLinks.length > 0 && (
                        <div className="border rounded-lg p-2 flex flex-wrap gap-2 mb-2 min-h-[50px]">
                            {youtubeLinks.map((link) => (
                                <span
                                    key={link}
                                    className="bg-red-100 text-red-700 px-2 py-1 rounded-full flex items-center text-sm"
                                >
                                    {link}
                                    <button
                                        onClick={() => removeYoutubeLink(link)}
                                        className="ml-2 hover:text-red-500"
                                    >
                                        <X className="w-4 h-4"/>
                                    </button>
                                </span>
                            ))}
                        </div>
                    )}

                    {/* YouTube Link Input */}
                    <input
                        type="text"
                        value={youtubeInput}
                        onChange={handleYoutubeInputChange}
                        placeholder="Paste YouTube video links, separate by comma"
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-200 outline-none text-black"
                    />
                </div>

                {/* Submit Status */}
                {submitStatus.status !== 'idle' && (
                    <div className={`mb-4 p-3 rounded-lg flex items-center ${
                        submitStatus.status === 'success'
                            ? 'bg-green-100 text-green-800'
                            : submitStatus.status === 'error'
                                ? 'bg-red-100 text-red-800'
                                : 'bg-yellow-100 text-yellow-800'
                    }`}>
                        {submitStatus.status === 'loading' && (
                            <span className="mr-2 animate-spin">⏳</span>
                        )}
                        {submitStatus.status === 'success' && (
                            <Check className="mr-2 w-5 h-5 text-green-600"/>
                        )}
                        {submitStatus.status === 'error' && (
                            <AlertTriangle className="mr-2 w-5 h-5 text-red-600"/>
                        )}
                        {submitStatus.message}
                    </div>
                )}

                {/* Submit Button */}
                <button
                    className="w-full bg-black text-white py-2 rounded-lg hover:bg-green-600 transition-colors flex items-center justify-center"
                    onClick={handleSubmit}
                    disabled={submitStatus.status === 'loading'}
                >
                    Add Video(s)
                </button>
            </div>
        </div>
    );
};

export default TagYoutubeModal;
