import React, {useEffect, useState} from 'react';
import VideoCard from "../VideoCard.tsx";
import type Video from "../../models/Video.ts";
import {getAllVideos, getVideosCountOfUser} from "../../services/VIdeoService.ts";

interface Props {
    initialPage: number;
    videosPerPage: number;
    onVideoClick: (video: Video) => void;
}

const Videos: React.FC<Props> = (props) => {
    const [page, setPage] = useState(props.initialPage);
    const [videos, setVideos] = useState<Video[]>([]);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const videosPerPage = props.videosPerPage;

    const fetchVideos = async () => {
        try {
            setIsLoading(true);

            // Fetch videos count
            const videosCount = await getVideosCountOfUser();
            if (!videosCount) {
                setError('Unable to retrieve video count');
                return;
            }

            // Calculate total pages
            const calculatedTotalPages = Math.ceil(videosCount / videosPerPage);
            setTotalPages(calculatedTotalPages);

            // Calculate skip
            const skip = (page - 1) * videosPerPage;

            // Fetch videos
            const fetchedVideos = await getAllVideos(skip, videosPerPage);

            if (!fetchedVideos || fetchedVideos.length < 1) {
                setError('No videos found');
                setVideos([]);
            } else {
                setVideos(fetchedVideos);
                setError(null);
            }
        } catch (err) {
            setError('Failed to load videos');
            setVideos([]);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchVideos();
    }, [page, videosPerPage]);

    const handlePageChange = (newPage: number) => {
        setPage(newPage);
    };

    // Render loading state
    if (isLoading) {
        return (
            <div className="flex justify-center items-center min-h-screen bg-gray-100">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-32 w-32 border-t-4 border-blue-500 mx-auto mb-4"></div>
                    <p className="text-2xl font-semibold text-gray-700">Loading Videos...</p>
                </div>
            </div>
        );
    }

    // Render error state
    if (error) {
        return (
            <div className="flex justify-center items-center min-h-screen bg-gray-100">
                <div className="text-center p-8 bg-white rounded-lg shadow-lg">
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="h-24 w-24 text-red-500 mx-auto mb-4"
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
                    <h2 className="text-3xl font-bold text-gray-800 mb-4">Oops!</h2>
                    <p className="text-xl text-gray-600 mb-6">{error}</p>
                    <button
                        onClick={() => fetchVideos()}
                        className="px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition duration-300"
                    >
                        Try Again
                    </button>
                </div>
            </div>
        );
    }

    // Render videos
    return (
        <div>
            <h2 className="text-center mt-8 text-2xl font-bold">Videos</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-3 gap-4 p-4 max-w-7xl mx-auto">
                {videos.map((video) => (
                    <VideoCard key={video.videoId} video={video}/>
                ))}
            </div>

            <div className="flex justify-center items-center mt-8 space-x-2">
                {page > 1 && (
                    <button
                        onClick={() => handlePageChange(page - 1)}
                        className="px-4 py-2 bg-gray-700 text-white rounded hover:bg-gray-600"
                    >
                        Previous
                    </button>
                )}

                {Array.from({length: totalPages}, (_, i) => i + 1).map((pageNum) => (
                    <button
                        key={pageNum}
                        onClick={() => handlePageChange(pageNum)}
                        className={`px-4 py-2 rounded ${
                            pageNum === page
                                ? 'bg-red-500 text-white'
                                : 'bg-gray-700 text-white hover:bg-gray-600'
                        }`}
                    >
                        {pageNum}
                    </button>
                ))}

                {page < totalPages && (
                    <button
                        onClick={() => handlePageChange(page + 1)}
                        className="px-4 py-2 bg-gray-700 text-white rounded hover:bg-gray-600"
                    >
                        Next
                    </button>
                )}
            </div>
        </div>
    );
};

export default Videos;