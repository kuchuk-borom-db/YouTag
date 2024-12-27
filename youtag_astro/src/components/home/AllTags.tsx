import React, {useEffect, useState} from "react";
import {getAllTags} from "../../pages/api/TagService.ts";

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
            const skip = (page - 1) * tagsPerPage;
            const result = await getAllTags(skip, tagsPerPage)


            // Get total tags count
            const tagsCount = result?.count || 0;
            console.log(`Tags count ${tagsCount}`)
            if (tagsCount === 0) {
                setError("No Tags Saved...");
                return;
            } else if (!tagsCount) {
                setError("Failed to fetch tags");
                return;
            }

            // Calculate total pages
            const calculatedTotalPages = Math.ceil(tagsCount / tagsPerPage);
            setTotalTagPages(calculatedTotalPages);

            // Calculate skip based on current page

            // Fetch tags
            const fetchedTags = await getAllTags(skip, tagsPerPage);
            console.log(`Fetched tags = ${fetchedTags}`);
            if (!fetchedTags || fetchedTags.tags.length < 1) {
                setTags([]); // Set to empty array instead of setting an error
            } else {
                setTags(fetchedTags.tags);
                setError(null); // Reset error if tags are found
            }
        } catch (err) {
            setError("Failed to load tags");
            setTags([]); // Ensure tags are cleared on error
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

            {/* Loading or No Tags State */}
            {isLoading ? (
                <div className="flex justify-center items-center p-6 bg-white rounded shadow-md">
                    <div className="flex flex-col items-center">
                        <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-blue-500 mb-2"></div>
                        <p className="text-lg font-semibold text-gray-700">Loading Tags...</p>
                    </div>
                </div>
            ) : (
                <>
                    {/* Error State */}
                    {error && (
                        <div className="flex justify-center items-center p-6 bg-red-100 text-red-600 rounded">
                            <p className="text-lg font-semibold">{error}</p>
                        </div>
                    )}

                    {/* No Tags Found State */}
                    {tags.length === 0 && !error && (
                        <div className="flex justify-center items-center p-6 bg-white rounded shadow-md">
                            <p className="text-lg font-semibold text-gray-700">No Tags Found</p>
                        </div>
                    )}

                    {/* Tags Grid */}
                    {tags.length > 0 && (
                        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
                            {tags.map((tag, index) => (
                                <div
                                    key={index}
                                    className="bg-gray-700 text-white py-2 px-4 rounded-full text-center cursor-pointer
                                    hover:bg-gray-600 transition duration-300 ease-in-out transform hover:scale-105"
                                    onClick={() => {
                                    window.location.href=`/search?tags=${tag}`
                                    }}
                                >
                                    {tag}
                                </div>
                            ))}
                        </div>
                    )}

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

                        {Array.from({length: totalTagPages}, (_, i) => i + 1).map((pageNum) => (
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
