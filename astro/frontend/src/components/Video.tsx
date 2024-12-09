import React, { useState } from 'react';
import type Video from "../models/Video.ts";
import { ChevronLeft, ChevronRight, Trash2, Plus } from 'lucide-react';
import TagYoutubeModal from './home/AddVideo.tsx';

interface VideoCardProps {
    video: Video;
}

const VideoCard: React.FC<VideoCardProps> = ({ video }) => {
    const [isYouTubeModalOpen, setIsYouTubeModalOpen] = useState(false);
    const [currentTagPage, setCurrentTagPage] = useState(0);
    const [tags, setTags] = useState(video.tags); // Local state for tags
    const tagsPerPage = 4;

    // Paginate tags
    const paginatedTags = tags.slice(
        currentTagPage * tagsPerPage,
        (currentTagPage + 1) * tagsPerPage
    );

    // Calculate total tag pages
    const totalTagPages = Math.ceil(tags.length / tagsPerPage);

    // Handle deleting a tag
    const handleDeleteTag = (tagToDelete: string) => {
        setTags((prevTags) => prevTags.filter((tag) => tag !== tagToDelete));
    };

    // Handle adding a new tag
    const handleAddTag = () => {
        setIsYouTubeModalOpen(true);
    };

    // Handle closing the modal
    const handleCloseModal = () => {
        setIsYouTubeModalOpen(false);
    };

    // Handle deleting the video
    const handleDeleteVideo = () => {
        console.log(`Video with ID ${video.videoId} deleted`);
        // Add your deletion logic here (e.g., API call, state update)
    };

    return (
        <div className="bg-white rounded-lg shadow hover:shadow-lg relative group">
            {/* Delete Video Icon */}
            <button
                onClick={handleDeleteVideo}
                className="absolute top-2 right-2 p-1 bg-red-100 hover:bg-red-200 rounded-full"
                title="Delete Video"
            >
                <Trash2 size={16} className="text-red-500" />
            </button>

            {/* Thumbnail */}
            <img
                src={video.thumbnailUrl}
                alt={video.title}
                className="w-full h-48 object-cover rounded-t-lg cursor-pointer"
                onClick={() => window.open(`https://www.youtube.com/watch?v=${video.videoId}`, '_blank')}
            />

            {/* Content */}
            <div className="p-4">
                <h3 className="text-lg font-bold text-gray-800 truncate">
                    {video.title}
                </h3>
                <p className="text-gray-500 text-sm truncate">
                    {video.description}
                </p>

                {/* Tags with Delete Icon */}
                <div className="mt-2 flex flex-wrap gap-2">
                    {paginatedTags.map((tag, index) => (
                        <div
                            key={index}
                            className="flex items-center gap-1 bg-blue-100 text-blue-800 px-2 py-1 rounded"
                        >
                            <span className="text-sm">{tag}</span>
                            <button
                                onClick={() => handleDeleteTag(tag)}
                                className="text-red-500 hover:text-red-600"
                                title="Delete Tag"
                            >
                                <Trash2 size={12} />
                            </button>
                        </div>
                    ))}
                </div>

                {/* Add Tag Button */}
                <div className="mt-2 flex items-center gap-2">
                    <button
                        onClick={handleAddTag}
                        className="flex items-center gap-1 px-3 py-1 text-sm bg-green-100 text-green-800 hover:bg-green-200 rounded"
                        title="Add Tag"
                    >
                        <Plus size={16} />
                        Add Tag
                    </button>
                </div>

                {/* Pagination */}
                {tags.length > tagsPerPage && (
                    <div className="mt-2 flex items-center justify-center gap-2">
                        <button
                            onClick={() =>
                                setCurrentTagPage(prev =>
                                    prev > 0 ? prev - 1 : totalTagPages - 1
                                )
                            }
                            className="p-1 bg-gray-100 hover:bg-gray-200 rounded-full"
                        >
                            <ChevronLeft size={16} />
                        </button>
                        <span className="text-xs text-gray-500">
                            {currentTagPage + 1}/{totalTagPages}
                        </span>
                        <button
                            onClick={() =>
                                setCurrentTagPage(prev =>
                                    prev < totalTagPages - 1 ? prev + 1 : 0
                                )
                            }
                            className="p-1 bg-gray-100 hover:bg-gray-200 rounded-full"
                        >
                            <ChevronRight size={16} />
                        </button>
                    </div>
                )}
            </div>

            {/* Tag YouTube Modal */}
            {isYouTubeModalOpen && (
                <TagYoutubeModal
                    initialTags={tags}
                    initialVideos={[video.videoId]}
                    onClose={handleCloseModal}
                />
            )}
        </div>
    );
};

export default VideoCard;