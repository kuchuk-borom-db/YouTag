import React, { useState } from 'react';
import { Plus, VideoIcon, Settings } from 'lucide-react';
import TagYoutubeModal from './AddVideo.tsx'; // Assuming this is in the same directory

const CreateContentButton: React.FC = () => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isYouTubeModalOpen, setIsYouTubeModalOpen] = useState(false);

    const toggleMenu = () => {
        setIsMenuOpen(!isMenuOpen);
    };

    const openYouTubeModal = () => {
        setIsMenuOpen(false);
        setIsYouTubeModalOpen(true);
    };

    const closeYouTubeModal = () => {
        setIsYouTubeModalOpen(false);
    };

    return (
        <div className="fixed right-6 bottom-6 z-50">
            {/* Create Content Button */}
            <div className="relative">
                {/* Expandable Menu */}
                {isMenuOpen && (
                    <div className="absolute bottom-full mb-4 right-0 flex flex-col space-y-2">
                        <button
                            onClick={openYouTubeModal}
                            className="flex items-center bg-white text-gray-800 px-4 py-2 rounded-lg shadow-lg hover:bg-gray-100 transition-colors"
                        >
                            <VideoIcon className="mr-2 w-5 h-5 text-red-500"/>
                            YouTube Video
                        </button>
                        <button
                            className="flex items-center bg-white text-gray-800 px-4 py-2 rounded-lg shadow-lg hover:bg-gray-100 transition-colors"
                        >
                            <Settings className="mr-2 w-5 h-5 text-gray-500"/>
                            Settings
                        </button>
                    </div>
                )}

                {/* Main Create Button */}
                <button
                    onClick={toggleMenu}
                    className="bg-blue-500 text-white w-16 h-16 rounded-full flex items-center justify-center shadow-2xl hover:bg-blue-600 transition-colors"
                >
                    <Plus className={`w-8 h-8 transition-transform ${isMenuOpen ? 'rotate-45' : ''}`} />
                </button>
            </div>

            {/* YouTube Modal (reusing the existing component) */}
            {isYouTubeModalOpen && <TagYoutubeModal onClose={closeYouTubeModal}/>}
        </div>
    );
};

export default CreateContentButton;