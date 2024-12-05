import React from 'react';
import type Video from "../models/Video.ts";

interface Props {
    video: Video;
    onClick?: (video: Video) => void;
    onTagClick?: (tag: string) => void;
}

const VideoCard: React.FC<Props> = ({
                                        video,
                                        onClick,
                                        onTagClick
                                    }) => {
    // Limit tags to a reasonable number
    const maxTagsToShow = 3; // Adjust based on space
    const tagsArray = Array.from(video.tags);
    const tagsToDisplay = tagsArray.slice(0, maxTagsToShow);
    const hasMoreTags = tagsArray.length > maxTagsToShow;

    // Truncate description for hover effect
    const maxDescriptionLength = 100; // Adjust to fit space
    const truncatedDescription =
        video.description.length > maxDescriptionLength
            ? video.description.slice(0, maxDescriptionLength) + '...'
            : video.description;

    // Handle video click
    const handleVideoClick = (e: React.MouseEvent) => {
        e.preventDefault();
        if (onClick) {
            onClick(video);
        } else {
            window.open(`https://www.youtube.com/watch?v=${video.videoId}`, '_blank');
        }
    };

    // Handle tag click
    const handleTagClick = (tag: string, e: React.MouseEvent) => {
        e.stopPropagation(); // Prevent video click
        if (onTagClick) {
            onTagClick(tag);
        }
    };

    return (
        <div
            className="video-card relative w-full max-w-sm h-64 bg-cover bg-center rounded-lg shadow-lg overflow-hidden group cursor-pointer"
            style={{backgroundImage: `url('${video.thumbnailUrl}')`}}
            onClick={handleVideoClick}
        >
            {/* Title and Tags (Always Visible) */}
            <div className="absolute inset-0 bg-black bg-opacity-50 flex flex-col justify-end p-4 text-white">
                <h3 className="text-lg font-bold truncate">{video.title}</h3>
                <div className="flex flex-wrap gap-2 mt-2">
                    {tagsToDisplay.map((tag, index) => (
                        <span
                            key={index}
                            className="bg-blue-500 bg-opacity-75 text-white text-xs px-2 py-1 rounded hover:bg-blue-600 transition duration-300"
                            onClick={(e) => handleTagClick(tag, e)}
                        >
                            {tag}
                        </span>
                    ))}
                    {hasMoreTags && (
                        <span className="bg-gray-500 bg-opacity-75 text-white text-xs px-2 py-1 rounded">
                            ...
                        </span>
                    )}
                </div>
            </div>

            {/* Hover Effect for Description */}
            <div
                className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-75 transition-all duration-300 ease-in-out flex items-center justify-center p-4">
                <p className="text-white text-sm leading-snug text-center opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                    {truncatedDescription}
                </p>
            </div>
        </div>
    );
};

export default VideoCard;