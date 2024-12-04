import React from 'react';
import type User from "../models/User.ts";

interface Prop {
    user: User
}

export const NavBar: React.FC<Prop> = ({user}) => {
    const handleProfileClick = () => {
        console.log("Profile clicked");
    };

    return (
        <nav className="flex items-center justify-between p-4 bg-white border-b border-gray-200"
             style={{boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)'}}>
            {/* Logo */}
            <div className="flex items-center space-x-4">
                <img
                    src="/youtag.png"
                    alt="Logo"
                    className="h-8 w-auto"
                />
            </div>

            {/* Search Field */}
            <div className="flex-grow mx-8 max-w-2xl">
                <div className="flex items-center">
                    <input
                        type="text"
                        placeholder="Search"
                        className="w-full px-4 py-2 border border-gray-300 rounded-l-full focus:outline-none focus:border-blue-500"
                    />
                    <button
                        className="bg-gray-100 border border-l-0 border-gray-300 px-4 py-2 rounded-r-full hover:bg-gray-200"
                    >
                        <img
                            src="/youtag.png"
                            alt="Search"
                            className="h-6 w-6"
                        />
                    </button>
                </div>
            </div>

            {/* User Profile */}
            <div className="flex items-center space-x-4">
                <img
                    id="user-profile"
                    src={user?.thumbnailUrl || '/profile.jpg'}
                    alt="Profile"
                    className="h-8 w-8 rounded-full cursor-pointer"
                    onClick={handleProfileClick}
                />
            </div>
        </nav>
    );
};

// Prop Types Validation
// NavBar.propTypes = {
//     user: PropTypes.shape({
//         profilePicture: PropTypes.string,
//     }),
// };
//
// NavBar.defaultProps = {
//     user: {
//         profilePicture: '/profile.jpg',
//     },
// };

export default NavBar;
