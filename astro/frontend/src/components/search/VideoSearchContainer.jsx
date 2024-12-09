import React, { useState } from 'react';
import SearchBox from '../SearchBox';
import VideosList from '../home/VideosList';

const VideoSearchContainer = () => {
    const [tags, setTags] = useState([]);

    return (
        <div className="bg-gray-100">
            <SearchBox
                profileLogoUrl={""}
                onSearch={(updatedTags) => {
                    console.log(`Tags updated ${updatedTags}`);
                    setTags(updatedTags);
                }}
            />

            <VideosList
                initialPage={1}
                videosPerPage={2}
                onVideoClick={()=>{}}
                tags={tags}
            />
        </div>
    );
};

export default VideoSearchContainer;