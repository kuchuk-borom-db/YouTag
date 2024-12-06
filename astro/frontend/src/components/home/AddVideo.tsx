import React, { type ChangeEvent, type KeyboardEvent, useRef, useState } from 'react';
import { Tag, X, Youtube } from 'lucide-react';

// Dummy tag suggestions (can be replaced with real data)
const TAG_SUGGESTIONS = [
    'music', 'tutorial', 'gaming', 'comedy', 'tech',
    'vlog', 'animation', 'review', 'education', 'travel',
    'fashion', 'sports', 'news', 'food', 'lifestyle',
    'science', 'photography', 'diy', 'business', 'health'
];

interface TagYoutubeModalProps {
    onClose: () => void;
    onSubmit: (tags: string[], youtubeLink: string) => void;
}

const TagYoutubeModal: React.FC<TagYoutubeModalProps> = ({ onClose, onSubmit }) => {
    const [tags, setTags] = useState<string[]>([]);
    const [inputValue, setInputValue] = useState('');
    const [youtubeLink, setYoutubeLink] = useState('');
    const [suggestions, setSuggestions] = useState<string[]>(TAG_SUGGESTIONS.slice(0, 10));
    const [currentPage, setCurrentPage] = useState(1);
    const [suggestionsPerPage] = useState(10);
    const inputRef = useRef<HTMLInputElement>(null);

    const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;

        // Check if last character is a comma to add tag
        if (value.endsWith(',')) {
            const newTag = value.slice(0, -1).trim();
            if (newTag && !tags.includes(newTag)) {
                setTags([...tags, newTag]);
                setInputValue('');
                setCurrentPage(1);
                updateSuggestions('');
            }
        }

        setInputValue(value);

        // Update suggestions based on input
        updateSuggestions(value.trim());
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
            updateSuggestions('');
        }
    };

    const handleSubmit = () => {
        onSubmit(tags, youtubeLink);
        onClose();
    };

    const updateSuggestions = (searchTerm: string) => {
        const filteredSuggestions = TAG_SUGGESTIONS.filter(
            tag =>
                tag.toLowerCase().includes(searchTerm.toLowerCase()) &&
                !tags.includes(tag)
        );

        const startIndex = (currentPage - 1) * suggestionsPerPage;
        const endIndex = startIndex + suggestionsPerPage;
        setSuggestions(filteredSuggestions.slice(startIndex, endIndex));
    };

    const handlePageChange = (pageNumber: number) => {
        setCurrentPage(pageNumber);
        updateSuggestions(inputValue.trim());
    };

    return (
        <div
            className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 animate-fade-in"
            onClick={onClose}
        >
            <div
                className="bg-white rounded-xl p-6 w-96 max-w-[90%] shadow-2xl animate-slide-up"
                onClick={(e) => e.stopPropagation()}
            >
                {/* Close Button */}
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 text-gray-500 hover:text-gray-700"
                >
                    <X className="w-6 h-6" />
                </button>

                {/* Tags Input Section */}
                <div className="mb-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2 items-center">
                        <Tag className="mr-2 w-5 h-5 text-blue-500" />
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
                                    <X className="w-4 h-4" />
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
                            className="flex-grow outline-none px-1 text-sm"
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
                            <div className="flex justify-center mt-4">
                                {Array.from({ length: Math.ceil(TAG_SUGGESTIONS.length / suggestionsPerPage) }, (_, index) => index + 1).map((pageNumber) => (
                                    <button
                                        key={pageNumber}
                                        onClick={() => handlePageChange(pageNumber)}
                                        className={`px-3 py-1 rounded-md ${
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

                {/* YouTube Link Input */}
                <div className="mb-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2 items-center">
                        <Youtube className="mr-2 w-5 h-5 text-red-500" />
                        YouTube Link
                    </label>
                    <input
                        type="text"
                        value={youtubeLink}
                        onChange={(e) => setYoutubeLink(e.target.value)}
                        placeholder="Paste YouTube video link"
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-200 outline-none"
                    />
                </div>

                {/* Submit Button */}
                <button
                    className="w-full bg-black text-white py-2 rounded-lg hover:bg-green-600 transition-colors flex items-center justify-center"
                    onClick={handleSubmit}
                >
                    Add Video
                </button>
            </div>
        </div>
    );
};

export default TagYoutubeModal;