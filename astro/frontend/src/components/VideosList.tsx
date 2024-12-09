import React from "react";
import type Video from "../models/Video.ts";

interface Props {
    videos?: Video[]; // Nullable videos
    totalVideos?: number; // Nullable total videos
    skip: number;
    onVideoClick: (video: Video) => void;
}

const VideoList: React.FC<Props> = ({ videos = [], totalVideos = 0, skip, onVideoClick }) => {
    const itemsPerPage = videos.length || 1; // Default to 1 to prevent division by zero
    const currentPage = Math.floor(skip / itemsPerPage) + 1;
    const totalPages = Math.ceil(totalVideos / itemsPerPage);

    return (
        <div className="p-4">
            {/* Video Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                {videos.length > 0 ? (
                    videos.map((video) => (
                        <div
                            key={video.videoId}
                            className="bg-white rounded-lg shadow hover:shadow-lg cursor-pointer"
                            onClick={() => onVideoClick(video)}
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
                    <button
                        className={`px-4 py-2 bg-gray-200 rounded ${
                            currentPage === 1 ? "cursor-not-allowed" : "hover:bg-gray-300"
                        }`}
                        onClick={() => {
                            if (currentPage > 1) {
                                const prevSkip = (currentPage - 2) * itemsPerPage;
                                if (videos[prevSkip]) onVideoClick(videos[prevSkip]);
                            }
                        }}
                        disabled={currentPage === 1}
                    >
                        Previous
                    </button>
                    {[...Array(totalPages)].map((_, index) => (
                        <button
                            key={index}
                            className={`px-4 py-2 rounded ${
                                index + 1 === currentPage
                                    ? "bg-red-500 text-white"
                                    : "bg-gray-200 hover:bg-gray-300"
                            }`}
                            onClick={() => {
                                const targetSkip = index * itemsPerPage;
                                if (videos[targetSkip]) onVideoClick(videos[targetSkip]);
                            }}
                        >
                            {index + 1}
                        </button>
                    ))}
                    <button
                        className={`px-4 py-2 bg-gray-200 rounded ${
                            currentPage === totalPages ? "cursor-not-allowed" : "hover:bg-gray-300"
                        }`}
                        onClick={() => {
                            if (currentPage < totalPages) {
                                const nextSkip = currentPage * itemsPerPage;
                                if (videos[nextSkip]) onVideoClick(videos[nextSkip]);
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
