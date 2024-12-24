import React, {useEffect, useRef, useState} from 'react';
import {ChevronLeft, ChevronRight, Tag, X} from 'lucide-react';
import {getAllTags, getTagsContainingKeyword} from "../pages/api/TagService.ts";

interface SearchComponentProps {
    initialTags: string[] | null
}

const SearchComponent: React.FC<SearchComponentProps> = ({initialTags}) => {
    console.log(`Initial tags = ${initialTags}`)
    const [tags, setTags] = useState<string[]>([]);
    const [tagInput, setTagInput] = useState<string>('');
    const [tagSuggestions, setTagSuggestions] = useState<string[]>([]);
    const [showTagSuggestions, setShowTagSuggestions] = useState(false);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);

    const tagSearchInputRef = useRef<HTMLInputElement>(null);
    const tagSuggestionsRef = useRef<HTMLDivElement>(null);
    const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    // Pagination settings
    const ITEMS_PER_PAGE = 2;

    // Initialize tags from initialTag prop
    useEffect(() => {
        if (initialTags && Array.isArray(initialTags) && initialTags.length > 0) {
            // Filter out any empty or null tags
            const validTags = initialTags.filter(tag => tag && tag.trim() !== '');

            if (validTags.length > 0) {
                setTags(validTags);
            }
        }
    }, [initialTags]);

    // Fetch tags based on input
    const fetchTags = async (keyword?: string) => {
        try {
            let tags: string[] = [];
            let totalCount = 0;

            if (keyword) {
                // If there's a keyword, fetch tags containing that keyword
                const result = await getTagsContainingKeyword(keyword);
                totalCount = result?.count || 0
                tags = result?.tags || []
            } else {
                // If no keyword, fetch all tags
                const result = await getAllTags((currentPage - 1) * ITEMS_PER_PAGE, ITEMS_PER_PAGE)
                totalCount = result?.count || 0;
                tags = result?.tags || [];
            }

            setTagSuggestions(tags);
            setTotalPages(Math.ceil(totalCount / ITEMS_PER_PAGE));

            // Always show suggestions if there are tags
            setShowTagSuggestions(tags.length > 0);
        } catch (error) {
            console.error('Error fetching tags:', error);
            setTagSuggestions([]);
            setShowTagSuggestions(false);
        }
    };

    // Initial load of tags or when input changes
    useEffect(() => {
        // Fetch tags when there's input or on initial load
        if (tagInput.trim()) {
            fetchTags(tagInput.trim());
        } else {
            fetchTags(); // Fetch all tags
        }
    }, [tagInput, currentPage]);

    // Debounced tag search function
    const debouncedTagSearch = (value: string) => {
        if (debounceTimerRef.current) {
            clearTimeout(debounceTimerRef.current);
        }

        debounceTimerRef.current = setTimeout(() => {
            if (value.trim()) {
                setCurrentPage(1);
                fetchTags(value.trim());
            } else {
                fetchTags(); // Fetch all tags if input is empty
            }
        }, 300);
    };

    // Handle tag input and automatic addition
    const handleTagSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        if (value.endsWith(',')) {
            const newTag = value.slice(0, value.length - 1).trim();
            if (newTag && !tags.includes(newTag)) {
                setTags(prevState => {
                    return [...prevState, newTag];
                });
                setTagInput('');
                setShowTagSuggestions(false);
            }
        } else {
            setTagInput(value);
            debouncedTagSearch(value);
        }
    };

    const removeTag = (tagToRemove: string) => {
        setTags(prev => prev.filter(tag => tag !== tagToRemove));
    };

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        console.log('Searching with:', {tags});
        setShowTagSuggestions(false);

        const url = new URL(window.location.href);
        url.searchParams.set('tags', tags.join(","));
        url.searchParams.set("page", "1")
        window.location.href = url.toString();
    };

    const handleTagSuggestionClick = (tag: string) => {
        if (!tags.includes(tag)) {
            setTags(prev => [...prev, tag]);
        }
        setTagInput('');
        setShowTagSuggestions(false);
    };

    const handlePrevPage = () => {
        if (currentPage > 1) {
            setCurrentPage(currentPage - 1);
        }
    };

    const handleNextPage = () => {
        if (currentPage < totalPages) {
            setCurrentPage(currentPage + 1);
        }
    };

    // Focus input to show suggestions
    const handleInputFocus = () => {
        // Show suggestions if there are any, regardless of input
        if (tagSuggestions.length > 0) {
            setShowTagSuggestions(true);
        }
    };

    return (
        <div className="bg-black/70 min-w-full w-full max-w-xl mx-auto p-4">
            <form onSubmit={handleSearch} className="space-y-4">
                <div className="relative" ref={tagSearchInputRef}>
                    <div className="relative">
                        <input
                            type="text"
                            placeholder="Enter Tags (comma-separated)"
                            value={tagInput}
                            onChange={handleTagSearchChange}
                            onFocus={handleInputFocus}
                            className="w-full pl-10 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-300"
                        />
                        <Tag className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20}/>
                    </div>

                    {tags.length > 0 && (
                        <div className="mt-2 flex flex-wrap gap-2">
                            {tags.map((tag, index) => (
                                <div
                                    key={index}
                                    className="bg-green-100 text-green-700 px-2 py-1 rounded-full flex items-center space-x-1"
                                >
                                    <span>{tag}</span>
                                    <button
                                        type="button"
                                        onClick={() => removeTag(tag)}
                                        className="text-green-500 hover:text-green-700"
                                    >
                                        <X size={16}/>
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}

                    {showTagSuggestions && (
                        <div
                            ref={tagSuggestionsRef}
                            className="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg"
                            onClick={(e) => e.stopPropagation()}
                        >
                            <div className="flex justify-between items-center p-2 border-b">
                                <span className="text-sm font-medium text-gray-700">
                                    {tagInput.trim() ? 'Tag Suggestions' : 'All Tags'}
                                </span>
                                <button
                                    type="button"
                                    onClick={() => setShowTagSuggestions(false)}
                                    className="text-gray-500 hover:text-gray-700 focus:outline-none"
                                >
                                    <X size={20}/>
                                </button>
                            </div>
                            <ul className="max-h-60 overflow-y-auto">
                                {tagSuggestions.map((tag, index) => (
                                    <li
                                        key={index}
                                        className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                                        onClick={() => handleTagSuggestionClick(tag)}
                                    >
                                        {tag}
                                    </li>
                                ))}
                            </ul>
                            <div className="flex justify-between items-center p-2 border-t">
                                <button
                                    type="button"
                                    onClick={handlePrevPage}
                                    className={`text-gray-500 hover:text-gray-700 focus:outline-none ${currentPage === 1 ? 'opacity-50 cursor-not-allowed' : ''}`}
                                    disabled={currentPage === 1}
                                >
                                    <ChevronLeft size={20}/>
                                </button>
                                <span className="text-sm text-gray-700">
                                    Page {currentPage} of {totalPages}
                                </span>
                                <button
                                    type="button"
                                    onClick={handleNextPage}
                                    className={`text-gray-500 hover:text-gray-700 focus:outline-none ${currentPage === totalPages ? 'opacity-50 cursor-not-allowed' : ''}`}
                                    disabled={currentPage === totalPages}
                                >
                                    <ChevronRight size={20}/>
                                </button>
                            </div>
                        </div>
                    )}
                </div>
                <button
                    type="submit"
                    className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors duration-300 flex items-center justify-center space-x-2"
                >
                    <Tag size={20}/>
                    <span>Search</span>
                </button>
            </form>
        </div>
    );
};

export default SearchComponent;