import React from "react";
import type Video from "../models/Video.ts";

interface Props {
    videos?: Video[];
    totalVideos: number;
    currentPage: number;
}

const VideoList: React.FC<Props> = ({videos = [], totalVideos = 0, currentPage}) => {
    // Use Math.ceil to round up the total pages and prevent floating-point results
    // Divide totalVideos by the current page's video count to get accurate pagination
    const totalPages = Math.ceil(totalVideos / (videos.length || 1));
    console.log("Total Pages passed to VideosList:", totalPages);

    return (
        <div className="p-4">
            {/* Video Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                {videos.length > 0 ? (
                    videos.map((video) => (
                        <div
                            key={video.videoId}
                            className="bg-white rounded-lg shadow hover:shadow-lg cursor-pointer"
                            onClick={() => {
                                console.log(`Video clicked ${video.videoId}`);
                            }}
                        >
                            <img
                                src={video.thumbnailUrl}
                                alt={video.title}
                                className="w-full h-48 object-cover rounded-t-lg"
                            />
                            <div className="p-4">
                                <h3 className="text-lg font-bold text-gray-800 truncate">
                                    {video.title}
                                </h3>
                                <p className="text-gray-500 text-sm truncate">
                                    {video.description}
                                </p>
                                <div className="mt-2 flex flex-wrap gap-2">
                                    {video.tags.map((tag, index) => (
                                        <span
                                            key={index}
                                            className="text-sm bg-blue-100 text-blue-800 px-2 py-1 rounded"
                                        >
                                            {tag}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        </div>
                    ))
                ) : (
                    <p className="text-center text-gray-500">No videos to display</p>
                )}
            </div>

            {/* Pagination */}
            {totalVideos > 0 && (
                <div className="flex justify-center items-center gap-4 mt-6">
                    {/* Previous Button */}
                    <button
                        className={`px-4 py-2 bg-gray-200 rounded ${
                            currentPage === 1 ? "cursor-not-allowed" : "hover:bg-gray-300"
                        }`}
                        onClick={() => {
                            if (currentPage > 1) {
                                window.location.href = `?page=${currentPage - 1}`;
                            }
                        }}
                        disabled={currentPage === 1}
                    >
                        Previous
                    </button>

                    {/* Page Numbers */}
                    {[...Array(totalPages)].map((_, index) => (
                        <button
                            key={index}
                            className={`px-4 py-2 rounded ${
                                index + 1 === currentPage
                                    ? "bg-red-500 text-white"
                                    : "bg-gray-200 hover:bg-gray-300"
                            }`}
                            onClick={() => {
                                if(index + 1 === currentPage) {
                                    console.log("Already in this page dude")
                                    return;
                                }
                                window.location.href = `?page=${index + 1}`;
                            }}
                        >
                            {index + 1}
                        </button>
                    ))}

                    {/* Next Button */}
                    <button
                        className={`px-4 py-2 bg-gray-200 rounded ${
                            currentPage === totalPages ? "cursor-not-allowed" : "hover:bg-gray-300"
                        }`}
                        onClick={() => {
                            if (currentPage < totalPages) {
                                window.location.href = `?page=${currentPage + 1}`;
                            }
                        }}
                        disabled={currentPage === totalPages}
                    >
                        Next
                    </button>
                </div>
            )}
        </div>
    );
};

export default VideoList;