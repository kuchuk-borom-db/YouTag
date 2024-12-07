import React, { useState, type FunctionComponent } from 'react';
import { Search, Tag, User } from 'lucide-react';

interface SearchComponentProps {
    profileLogoUrl: string | null
}

const SearchComponent: FunctionComponent<SearchComponentProps> = ({ profileLogoUrl }) => {
    const [titleSearch, setTitleSearch] = useState<string>('');
    const [tagSearch, setTagSearch] = useState<string>('');

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        console.log('Searching with:', { titleSearch, tagSearch });
    };

    return (
        <div className="bg-gray-100 w-full">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex flex-col md:flex-row items-center justify-between space-y-4 md:space-y-0">
                <div className="flex items-center w-full md:w-auto justify-between">
                    <img src={"youtag.png"} alt="Logo" className="h-8 mr-4" />
                    <div className="md:hidden">
                        <img src={!profileLogoUrl? "profile.jpg" : profileLogoUrl} className="h-8 rounded-full"  alt={"profile.jpg"}/>
                    </div>
                </div>
                <form
                    onSubmit={handleSearch}
                    className="w-full flex flex-col md:flex-row items-center space-y-4 md:space-y-0 md:space-x-4"
                >
                    <div className="relative w-full md:flex-1">
                        <input
                            type="text"
                            placeholder="Search Titles"
                            value={titleSearch}
                            onChange={(e) => setTitleSearch(e.target.value)}
                            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-300"
                        />
                        <Search
                            className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"
                            size={20}
                        />
                    </div>
                    <div className="relative w-full md:flex-1">
                        <input
                            type="text"
                            placeholder="Search Tags"
                            value={tagSearch}
                            onChange={(e) => setTagSearch(e.target.value)}
                            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-300"
                        />
                        <Tag
                            className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"
                            size={20}
                        />
                    </div>
                    <button
                        type="submit"
                        className="w-full md:w-auto bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors duration-300 flex items-center justify-center space-x-2"
                    >
                        <Search size={20} />
                        <span>Search</span>
                    </button>
                </form>
                <div className="hidden md:flex items-center ">
                    <img src={!profileLogoUrl ? "profile.jpg" : profileLogoUrl} className="h-8 rounded-full"
                         alt={"profile.jpg"}/>
                </div>
            </div>
        </div>
    );
};

export default SearchComponent;