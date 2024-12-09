import React, { useState, useEffect } from "react";
import { getAllTags, getTagCountOfUser } from "../../services/TagService.ts";

interface TagsProps {
    initialPage: number;
    tagsPerPage: number;
    onTagClick?: (tag: string) => void;
}

const AllTags: React.FC<TagsProps> = ({
                                          initialPage = 1,
                                          tagsPerPage = 5,
                                          onTagClick,
                                      }) => {
    const [page, setPage] = useState(initialPage);
    const [tags, setTags] = useState<string[]>([]);
    const [totalTagPages, setTotalTagPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchTags = async () => {
        try {
            setIsLoading(true);

            // Get total tags count
            const tagsCount = await getTagCountOfUser();
            if (!tagsCount) {
                setError("Failed to retrieve tag count");
                return;
            }

            // Calculate total pages
            const calculatedTotalPages = Math.ceil(tagsCount / tagsPerPage);
            setTotalTagPages(calculatedTotalPages);

            // Calculate skip based on current page
            const skip = (page - 1) * tagsPerPage;

            // Fetch tags
            const fetchedTags = await getAllTags(skip, tagsPerPage);

            if (!fetchedTags || fetchedTags.length < 1) {
                setError("No tags found");
                setTags([]);
            } else {
                setTags(fetchedTags);
                setError(null);
            }
        } catch (err) {
            setError("Failed to load tags");
            setTags([]);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchTags();
    }, [page, tagsPerPage]);

    return (
        <div className="container mx-auto px-4">
            <h2 className="text-center text-2xl font-bold my-6">All Tags</h2>

            {/* Loading or Error State */}
            {isLoading || error ? (
                <div className="flex justify-center items-center p-6 bg-white rounded shadow-md">
                    {isLoading ? (
                        <div className="flex flex-col items-center">
                            <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-blue-500 mb-2"></div>
                            <p className="text-lg font-semibold text-gray-700">Loading Tags...</p>
                        </div>
                    ) : (
                        <div className="flex flex-col items-center">
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                className="h-16 w-16 text-red-500 mb-2"
                                fill="none"
                                viewBox="0 0 24 24"
                                stroke="currentColor"
                            >
                                <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    strokeWidth={2}
                                    d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                                />
                            </svg>
                            <p className="text-lg font-semibold text-gray-700">{error}</p>
                            <button
                                onClick={fetchTags}
                                className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 mt-4 transition duration-300"
                            >
                                Try Again
                            </button>
                        </div>
                    )}
                </div>
            ) : (
                <>
                    {/* Tags Grid */}
                    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
                        {tags.map((tag, index) => (
                            <div
                                key={index}
                                className="bg-gray-700 text-white py-2 px-4 rounded-full text-center cursor-pointer
                            hover:bg-gray-600 transition duration-300 ease-in-out transform hover:scale-105"
                                onClick={() => onTagClick && onTagClick(tag)}
                            >
                                {tag}
                            </div>
                        ))}
                    </div>

                    {/* Pagination */}
                    <div className="flex justify-center items-center space-x-2 mt-8">
                        {page > 1 && (
                            <button
                                onClick={() => setPage(page - 1)}
                                className="px-4 py-2 bg-gray-700 text-white rounded hover:bg-gray-600"
                            >
                                Previous
                            </button>
                        )}

                        {Array.from({ length: totalTagPages }, (_, i) => i + 1).map((pageNum) => (
                            <button
                                key={pageNum}
                                onClick={() => setPage(pageNum)}
                                className={`px-4 py-2 rounded ${
                                    pageNum === page
                                        ? "bg-red-500 text-white"
                                        : "bg-gray-700 text-white hover:bg-gray-600"
                                }`}
                            >
                                {pageNum}
                            </button>
                        ))}

                        {page < totalTagPages && (
                            <button
                                onClick={() => setPage(page + 1)}
                                className="px-4 py-2 bg-gray-700 text-white rounded hover:bg-gray-600"
                            >
                                Next
                            </button>
                        )}
                    </div>
                </>
            )}
        </div>
    );
};

export default AllTags;
