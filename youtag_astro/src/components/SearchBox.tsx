import React, {useEffect, useRef, useState} from 'react';
import {ChevronLeft, ChevronRight, Tag, X} from 'lucide-react';
import {getAllTags, getTagsContainingKeyword} from "../pages/api/TagService.ts";

interface SearchComponentProps {
    initialTags: string[] | null
}

const SearchComponent: React.FC<SearchComponentProps> = ({initialTags}) => {
    const [tags, setTags] = useState<string[]>([]);
    const [tagInput, setTagInput] = useState<string>('');
    const [tagSuggestions, setTagSuggestions] = useState<string[]>([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);

    const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const ITEMS_PER_PAGE = 6;

    useEffect(() => {
        if (initialTags?.length) {
            setTags(initialTags.filter(tag => tag?.trim()));
        }
    }, [initialTags]);

    const fetchTags = async (keyword?: string) => {
        try {
            const result = keyword
                ? await getTagsContainingKeyword(keyword)
                : await getAllTags((currentPage - 1) * ITEMS_PER_PAGE, ITEMS_PER_PAGE);

            setTagSuggestions(result?.tags || []);
            setTotalPages(Math.ceil((result?.count || 0) / ITEMS_PER_PAGE));
        } catch (error) {
            console.error('Error fetching tags:', error);
            setTagSuggestions([]);
        }
    };

    useEffect(() => {
        fetchTags(tagInput.trim());
    }, [tagInput, currentPage]);

    const debouncedTagSearch = (value: string) => {
        if (debounceTimerRef.current) {
            clearTimeout(debounceTimerRef.current);
        }
        debounceTimerRef.current = setTimeout(() => {
            setCurrentPage(1);
            fetchTags(value.trim());
        }, 300);
    };

    const handleTagSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        if (value.endsWith(',')) {
            const newTag = value.slice(0, -1).trim();
            if (newTag && !tags.includes(newTag)) {
                setTags(prev => [...prev, newTag]);
            }
            setTagInput('');
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
        const url = new URL(window.location.href);
        url.searchParams.set('tags', tags.join(","));
        url.searchParams.set("page", "1");
        window.location.href = url.toString();
    };

    const handleTagSuggestionClick = (tag: string) => {
        if (!tags.includes(tag)) {
            setTags(prev => [...prev, tag]);
        }
        setTagInput('');
    };

    return (
        <div className="w-full max-w-xl mx-auto p-2 sm:p-4">
            <form onSubmit={handleSearch} className="space-y-2 sm:space-y-4">
                <div className="space-y-2 sm:space-y-4">
                    {/* Search Input */}
                    <div className="relative">
                        <input
                            type="text"
                            placeholder="Enter Tags (comma-separated)"
                            value={tagInput}
                            onChange={handleTagSearchChange}
                            className="w-full pl-8 sm:pl-10 py-1.5 sm:py-2 text-sm sm:text-base border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-300"
                        />
                        <Tag className="absolute left-2 sm:left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={16} />
                    </div>

                    {/* Selected Tags */}
                    {tags.length > 0 && (
                        <div className="flex flex-wrap gap-1.5 sm:gap-2">
                            {tags.map((tag, index) => (
                                <div
                                    key={index}
                                    className="bg-green-100 text-green-700 px-2 py-0.5 sm:py-1 rounded-full flex items-center text-sm sm:text-base"
                                >
                                    <span className="mr-1">{tag}</span>
                                    <button
                                        type="button"
                                        onClick={() => removeTag(tag)}
                                        className="text-green-500 hover:text-green-700 p-0.5"
                                    >
                                        <X size={14} />
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}

                    {/* Tags Suggestion Box */}
                    <div className="bg-white border border-gray-300 rounded-lg shadow-lg text-sm sm:text-base">
                        <div className="flex justify-between items-center p-2 border-b">
                            <span className="text-xs sm:text-sm font-medium text-gray-700">
                                {tagInput.trim() ? 'Tag Suggestions' : 'All Tags'}
                            </span>
                        </div>
                        <ul className="max-h-40 sm:max-h-60 overflow-y-auto">
                            {tagSuggestions.map((tag, index) => (
                                <li
                                    key={index}
                                    className="px-3 sm:px-4 py-1.5 sm:py-2 hover:bg-gray-100 cursor-pointer text-sm sm:text-base"
                                    onClick={() => handleTagSuggestionClick(tag)}
                                >
                                    {tag}
                                </li>
                            ))}
                        </ul>
                        {/* Pagination */}
                        <div className="flex justify-between items-center p-2 border-t text-xs sm:text-sm">
                            <button
                                type="button"
                                onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                                className={`text-gray-500 hover:text-gray-700 focus:outline-none p-1 ${currentPage === 1 ? 'opacity-50 cursor-not-allowed' : ''}`}
                                disabled={currentPage === 1}
                            >
                                <ChevronLeft size={16} />
                            </button>
                            <span className="text-gray-700">
                                Page {currentPage} of {totalPages}
                            </span>
                            <button
                                type="button"
                                onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
                                className={`text-gray-500 hover:text-gray-700 focus:outline-none p-1 ${currentPage === totalPages ? 'opacity-50 cursor-not-allowed' : ''}`}
                                disabled={currentPage === totalPages}
                            >
                                <ChevronRight size={16} />
                            </button>
                        </div>
                    </div>
                </div>

                {/* Search Button */}
                <button
                    type="submit"
                    className="w-full bg-blue-600 text-white py-1.5 sm:py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors duration-300 flex items-center justify-center space-x-2 text-sm sm:text-base"
                >
                    <Tag size={16} className="sm:w-5 sm:h-5" />
                    <span>Search</span>
                </button>
            </form>
        </div>
    );
};

export default SearchComponent;