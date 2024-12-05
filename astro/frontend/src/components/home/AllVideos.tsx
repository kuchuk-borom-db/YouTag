import React, {type SetStateAction, useState} from 'react';
import VideoCard from "../VideoCard.tsx";
import type Video from "../../models/Video.ts";
import {getTagCountOfUser} from "../../services/TagService.ts";

const dummyVideos: Video[] = [
    {
        videoId: "yt001",
        title: "Web Development Crash Course 2024",
        description: "Comprehensive guide to becoming a full-stack web developer in 2024. Learn the latest technologies and best practices.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Web+Dev+Course",
        tags: ["Web Development", "Programming", "Tutorial"],
    },
    {
        videoId: "yt002",
        title: "React Hooks Ultimate Guide",
        description: "Master React Hooks with practical examples and in-depth explanations. From useState to useContext and beyond!",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=React+Hooks",
        tags: ["React", "JavaScript", "Frontend"],
    },
    {
        videoId: "yt003",
        title: "Machine Learning for Beginners",
        description: "Start your AI journey with this beginner-friendly machine learning tutorial. Learn core concepts and practical applications.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Machine+Learning",
        tags: ["Machine Learning", "AI", "Data Science"],
    },
    {
        videoId: "yt004",
        title: "Python Automation Masterclass",
        description: "Learn how to automate boring tasks with Python. From simple scripts to complex workflows, this course covers it all.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Python+Automation",
        tags: ["Python", "Automation", "Programming"],
    },
    {
        videoId: "yt005",
        title: "Design Thinking Workshop",
        description: "Unlock your creative problem-solving skills with this comprehensive design thinking workshop for professionals.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Design+Thinking",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt006",
        title: "Cybersecurity Essentials",
        description: "Protect yourself and your organization with this comprehensive cybersecurity course covering latest threats and defenses.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Cybersecurity",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt007",
        title: "Blockchain and Cryptocurrency Explained",
        description: "Deep dive into blockchain technology, cryptocurrencies, and their potential to revolutionize finance and beyond.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Blockchain+Crypto",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt008",
        title: "Cloud Computing Fundamentals",
        description: "Comprehensive guide to cloud computing, covering AWS, Azure, and Google Cloud platforms with practical examples.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Cloud+Computing",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt009",
        title: "Mobile App Development with Flutter",
        description: "Learn to build cross-platform mobile applications using Flutter and Dart. Create beautiful, performant apps for iOS and Android.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Flutter+Development",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt010",
        title: "Data Visualization Techniques",
        description: "Master the art of data visualization using tools like D3.js, Matplotlib, and Tableau. Transform raw data into compelling stories.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Data+Visualization",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt011",
        title: "Advanced SQL Mastery",
        description: "Take your database skills to the next level. Learn advanced SQL techniques, query optimization, and database design principles.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Advanced+SQL",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt012",
        title: "Ethical Hacking and Penetration Testing",
        description: "Learn the techniques used by ethical hackers to identify and fix vulnerabilities in computer systems and networks.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Ethical+Hacking",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt013",
        title: "DevOps and Continuous Integration",
        description: "Comprehensive course on DevOps practices, CI/CD pipelines, Docker, Kubernetes, and modern software deployment strategies.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=DevOps+CI%2FCD",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt014",
        title: "Natural Language Processing with Python",
        description: "Dive into the world of NLP. Learn text analysis, sentiment detection, and building intelligent language processing applications.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=NLP+Python",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt015",
        title: "Responsive Web Design Masterclass",
        description: "Create stunning, mobile-friendly websites using modern CSS techniques, Flexbox, Grid, and responsive design principles.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Responsive+Design",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt016",
        title: "Game Development with Unity",
        description: "Learn to create professional video games using Unity game engine. Cover 2D and 3D game development techniques.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Unity+Game+Dev",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt017",
        title: "Artificial Intelligence Ethics",
        description: "Explore the ethical implications of AI technologies, discussing bias, privacy, and the societal impact of intelligent systems.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=AI+Ethics",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt018",
        title: "Serverless Computing Fundamentals",
        description: "Understand serverless architecture, AWS Lambda, Azure Functions, and how to build scalable applications without managing servers.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Serverless+Computing",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt019",
        title: "GraphQL API Development",
        description: "Master GraphQL for building flexible and efficient APIs. Learn schema design, resolvers, and best practices.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=GraphQL+APIs",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    },
    {
        videoId: "yt020",
        title: "Quantum Computing Basics",
        description: "Introduction to quantum computing concepts, quantum algorithms, and the potential future of computational technology.",
        thumbnailUrl: "https://via.placeholder.com/640x360.png?text=Quantum+Computing",
        tags: ["Serverless", "Cloud", "Backend", "Tutorial", "Kuchuk"],
    }
]

interface Props {
    initialPage: number;
    videosPerPage: number;
}

const Videos: React.FC<Props> = (props) => {
    const [page, setPage] = useState(props.initialPage);
    const videosPerPage = props.videosPerPage;

    const totalTagsCount = await getTagCountOfUser();

    if (!totalTagsCount) {
        return (<h1>Total Tag count was null</h1>)
    }

    const totalPages = Math.ceil(totalTagsCount / videosPerPage);

    const handlePageChange = (newPage: SetStateAction<number>) => {
        setPage(newPage);
    };

    //TODO Complete paginated video and change tag count to video count
    return (
        <div>
            <h2 className="text-center mt-8 text-2xl font-bold">Videos</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-3 gap-4 p-4 max-w-7xl mx-auto">
                {paginatedVideos.map((video) => (
                    <VideoCard key={video.videoId} video={video}/>
                ))}
            </div>

            <div className="flex justify-center items-center mt-8 space-x-2">
                {page > 1 && (
                    <button
                        onClick={() => {
                            console.log("Clicked on page");
                            console.log(`Cookies = ${document.cookie}`);
                            handlePageChange(page - 1)
                        }}
                        className="px-4 py-2 bg-gray-700 text-white rounded hover:bg-gray-600"
                    >
                        Previous
                    </button>
                )}

                {Array.from({length: totalPages}, (_, i) => i + 1).map((pageNum) => (
                    <button
                        key={pageNum}
                        onClick={() => handlePageChange(pageNum)}
                        className={`px-4 py-2 rounded ${
                            pageNum === page
                                ? 'bg-red-500 text-white'
                                : 'bg-gray-700 text-white hover:bg-gray-600'
                        }`}
                    >
                        {pageNum}
                    </button>
                ))}

                {page < totalPages && (
                    <button
                        onClick={() => handlePageChange(page + 1)}
                        className="px-4 py-2 bg-gray-700 text-white rounded hover:bg-gray-600"
                    >
                        Next
                    </button>
                )}
            </div>
        </div>
    );
};

export default Videos;