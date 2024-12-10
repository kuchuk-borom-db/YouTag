// NavBar.tsx
import React from 'react';
import type User from "../models/User.ts";
import { Search } from 'lucide-react';

interface Prop {
    user: User
}

export const NavBar: React.FC<Prop> = ({user}) => {
    const handleProfileClick = () => {

        window.location.href = "/profile";
    };

    const handleSearchClick = () => {
        window.location.href = "/search";
    };

    const handleAboutClick = () => {
        window.location.href = "/about";
    };

    return (
        <nav className="flex items-center justify-between p-4 bg-white border-b border-gray-200" style={{boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)'}}>
            <div className="flex items-center space-x-4">
                <img src="/youtag.png" alt="Logo" className="h-8 w-auto" />
                <button className="text-gray-600 hover:text-gray-800" onClick={handleAboutClick}>
                    About
                </button>
            </div>

            <div className="flex items-center space-x-4">
                <button onClick={handleSearchClick} className="bg-gray-100 hover:bg-gray-200 p-2 rounded-full" title="Search">
                    <Search size={20} className="text-gray-600" />
                </button>
                <img id="user-profile" src={user?.thumbnailUrl || '/profile.jpg'} alt="Profile" className="h-8 w-8 rounded-full cursor-pointer" onClick={handleProfileClick} />
            </div>
        </nav>
    );
};

export default NavBar;