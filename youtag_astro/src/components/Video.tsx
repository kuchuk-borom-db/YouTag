import React, {useState} from 'react';
import type Video from "../models/Video.ts";
import {Check, ChevronLeft, ChevronRight, Plus, Trash2, X} from 'lucide-react';
import TagYoutubeModal from './home/AddVideo.tsx';
import Cookies from "js-cookie";
import {deleteTagFromVideo} from "../pages/api/TagService.ts";
import {deleteVideo} from "../pages/api/VIdeoService.ts";

interface VideoCardProps {
    video: Video;
}

const VideoCard: React.FC<VideoCardProps> = ({video}) => {
    const [isYouTubeModalOpen, setIsYouTubeModalOpen] = useState(false);
    const [currentTagPage, setCurrentTagPage] = useState(0);
    const [tags, setTags] = useState(video.tags);
    const [isDeleteMode, setIsDeleteMode] = useState(false);
    const [selectedTags, setSelectedTags] = useState<string[]>([]);
    const tagsPerPage = 4;

    // Paginate tags
    const paginatedTags = tags.slice(
        currentTagPage * tagsPerPage,
        (currentTagPage + 1) * tagsPerPage
    );

    // Calculate total tag pages
    const totalTagPages = Math.ceil(tags.length / tagsPerPage);

    // Handle bulk tag selection in delete mode
    const handleTagSelect = (tag: string) => {
        if (isDeleteMode) {
            setSelectedTags(prev =>
                prev.includes(tag)
                    ? prev.filter(t => t !== tag)
                    : [...prev, tag]
            );
        }
    };

    // Handle bulk tag deletion
    const handleBulkDeleteTags = async () => {
        try {
            await deleteTagFromVideo(selectedTags, [video.videoId], Cookies.get("token")!);
            // Update local state after successful deletion
            setTags(prevTags => prevTags.filter(tag => !selectedTags.includes(tag)));
            setSelectedTags([]);
            setIsDeleteMode(false);
        } catch (error) {
            console.error("Failed to delete tags:", error);
            // You might want to show an error message to the user here
        }
    };

    // Handle canceling tag deletion mode
    const handleCancelDeleteMode = () => {
        setIsDeleteMode(false);
        setSelectedTags([]);
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
    const handleDeleteVideo = async () => {
        console.log(`Video with ID ${video.videoId} deleted`);
        await deleteVideo(video.videoId, Cookies.get("token")!);
        window.location.reload();
    };

    return (
        <div className="bg-white rounded-lg shadow hover:shadow-lg relative group">
            {/* Delete Video Icon */}
            <button
                onClick={handleDeleteVideo}
                className="absolute top-2 right-2 p-1 bg-red-100 hover:bg-red-200 rounded-full"
                title="Delete Video"
            >
                <Trash2 size={16} className="text-red-500"/>
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
                    {video.author}
                </p>

                {/* Tags with Bulk Select */}
                <div className="mt-2 flex flex-wrap gap-2">
                    {paginatedTags.map((tag, index) => (
                        <div
                            key={index}
                            onClick={() => handleTagSelect(tag)}
                            className={`flex items-center gap-1 px-2 py-1 rounded cursor-pointer ${
                                isDeleteMode
                                    ? (selectedTags.includes(tag)
                                        ? 'bg-red-200 text-red-800'
                                        : 'bg-blue-100 text-blue-800')
                                    : 'bg-blue-100 text-blue-800'
                            }`}
                        >
                            <span className="text-sm">{tag}</span>
                            {isDeleteMode && selectedTags.includes(tag) && (
                                <Check size={12} className="ml-1"/>
                            )}
                        </div>
                    ))}
                </div>

                {/* Tag Management Buttons */}
                <div className="mt-2 flex items-center gap-2">
                    {!isDeleteMode ? (
                        <>
                            <button
                                onClick={handleAddTag}
                                className="flex items-center gap-1 px-3 py-1 text-sm bg-green-100 text-green-800 hover:bg-green-200 rounded"
                                title="Add Tag"
                            >
                                <Plus size={16}/>
                                Add Tag
                            </button>
                            {tags.length > 0 && (
                                <button
                                    onClick={() => setIsDeleteMode(true)}
                                    className="flex items-center gap-1 px-3 py-1 text-sm bg-red-100 text-red-800 hover:bg-red-200 rounded"
                                    title="Delete Tags"
                                >
                                    <Trash2 size={16}/>
                                    Delete Tags
                                </button>
                            )}
                        </>
                    ) : (
                        <div className="flex items-center gap-2">
                            <button
                                onClick={handleBulkDeleteTags}
                                disabled={selectedTags.length === 0}
                                className={`flex items-center gap-1 px-3 py-1 text-sm rounded ${
                                    selectedTags.length > 0
                                        ? 'bg-red-500 text-white hover:bg-red-600'
                                        : 'bg-gray-200 text-gray-400 cursor-not-allowed'
                                }`}
                                title="Confirm Delete Selected Tags"
                            >
                                <Trash2 size={16}/>
                                Delete {selectedTags.length > 0 ? `(${selectedTags.length})` : ''}
                            </button>
                            <button
                                onClick={handleCancelDeleteMode}
                                className="flex items-center gap-1 px-3 py-1 text-sm bg-gray-100 text-gray-800 hover:bg-gray-200 rounded"
                                title="Cancel Tag Deletion"
                            >
                                <X size={16}/>
                                Cancel
                            </button>
                        </div>
                    )}
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
                            <ChevronLeft size={16}/>
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
                            <ChevronRight size={16}/>
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