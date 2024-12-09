import React from "react";
import type Video from "../models/Video.ts";
import VideoCard from "./Video.tsx";

interface Props {
    videos?: Video[];
    totalVideos: number;
    currentPage: number;
    videosPerPage: number;
    onDelete?: (videoId: string) => void;
    onEdit?: (video: Video) => void;
}

const VideoList: React.FC<Props> = ({
                                        videos = [],
                                        totalVideos = 0,
                                        currentPage,
                                        videosPerPage = 1,
                                    }) => {
    const totalPages = Math.ceil(totalVideos / videosPerPage);

    return (
        <div className="p-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                {videos.length > 0 ? (
                    videos.map((video) => (
                        <VideoCard
                            key={video.videoId}
                            video={video}
                        />
                    ))
                ) : (
                    <p className="text-center text-gray-500">No videos to display</p>
                )}
            </div>

            {/* Pagination remains unchanged */}
            {totalVideos > 0 && (
                <div className="flex justify-center items-center gap-4 mt-6">
                    <button
                        className={`px-4 py-2 bg-gray-200 rounded ${
                            currentPage === 1 ? "cursor-not-allowed" : "hover:bg-gray-300"
                        }`}
                        onClick={() => {
                            if (currentPage > 1) {
                                const url = new URL(window.location.href);
                                url.searchParams.set('page', String(currentPage - 1));
                                window.location.href = url.toString();
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
                                if (index + 1 === currentPage) return;
                                const url = new URL(window.location.href);
                                url.searchParams.set('page', String(index + 1));
                                window.location.href = url.toString();
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
                                const url = new URL(window.location.href);
                                url.searchParams.set('page', String(currentPage + 1));
                                window.location.href = url.toString();
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