import React from "react";

interface TagsProps {
    userTags: string[];
    currentPage: number;
    tagsPerPage: number;
}

const AllTags: React.FC<TagsProps> = ({ userTags, currentPage, tagsPerPage }) => {
    // Calculate pagination details
    const startTagIndex = (currentPage - 1) * tagsPerPage;
    const endTagIndex = startTagIndex + tagsPerPage;
    const paginatedTags = userTags.slice(startTagIndex, endTagIndex);
    const totalTagPages = Math.ceil(userTags.length / tagsPerPage);

    // Styles
    const styles = {
        container: {
            textAlign: "center" as const,
            marginTop: "1rem",
        },
        grid: {
            display: "grid",
            gridTemplateColumns: "repeat(auto-fill, minmax(200px, 1fr))",
            gap: "1rem",
            padding: "1rem",
            maxWidth: "1400px",
            margin: "0 auto",
        },
        chip: {
            backgroundColor: "#333",
            color: "white",
            padding: "0.5rem 1rem",
            borderRadius: "20px",
            textAlign: "center" as const,
            transition: "background-color 0.3s",
            cursor: "pointer",
        },
        chipHover: {
            backgroundColor: "#555",
        },
        pagination: {
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            marginTop: "2rem",
            gap: "0.5rem",
        },
        paginationBtn: {
            backgroundColor: "#333",
            color: "white",
            border: "none",
            padding: "0.5rem 1rem",
            borderRadius: "4px",
            cursor: "pointer",
            transition: "background-color 0.3s",
        },
        paginationBtnHover: {
            backgroundColor: "#555",
        },
        activeBtn: {
            backgroundColor: "#ff0000",
        },
    };

    return (
        <div>
            <h2 style={styles.container}>All Tags</h2>
            <div style={styles.grid}>
                {paginatedTags.map((tag, index) => (
                    <div
                        style={styles.chip}
                        key={index}
                        onMouseOver={(e) =>
                            ((e.target as HTMLElement).style.backgroundColor =
                                styles.chipHover.backgroundColor)
                        }
                        onMouseOut={(e) =>
                            ((e.target as HTMLElement).style.backgroundColor =
                                styles.chip.backgroundColor)
                        }
                    >
                        {tag}
                    </div>
                ))}
            </div>

            <div style={styles.pagination}>
                {currentPage > 1 && (
                    <a
                        href={`?tagPage=${currentPage - 1}`}
                        style={styles.paginationBtn}
                        onMouseOver={(e) =>
                            ((e.target as HTMLElement).style.backgroundColor =
                                styles.paginationBtnHover.backgroundColor)
                        }
                        onMouseOut={(e) =>
                            ((e.target as HTMLElement).style.backgroundColor =
                                styles.paginationBtn.backgroundColor)
                        }
                    >
                        Previous
                    </a>
                )}

                {Array.from({ length: totalTagPages }, (_, i) => i + 1).map((pageNum) => (
                    <a
                        href={`?tagPage=${pageNum}`}
                        key={pageNum}
                        style={{
                            ...styles.paginationBtn,
                            ...(pageNum === currentPage ? styles.activeBtn : {}),
                        }}
                        onMouseOver={(e) =>
                            ((e.target as HTMLElement).style.backgroundColor =
                                pageNum === currentPage
                                    ? styles.activeBtn.backgroundColor
                                    : styles.paginationBtnHover.backgroundColor)
                        }
                        onMouseOut={(e) =>
                            ((e.target as HTMLElement).style.backgroundColor =
                                pageNum === currentPage
                                    ? styles.activeBtn.backgroundColor
                                    : styles.paginationBtn.backgroundColor)
                        }
                    >
                        {pageNum}
                    </a>
                ))}

                {currentPage < totalTagPages && (
                    <a
                        href={`?tagPage=${currentPage + 1}`}
                        style={styles.paginationBtn}
                        onMouseOver={(e) =>
                            ((e.target as HTMLElement).style.backgroundColor =
                                styles.paginationBtnHover.backgroundColor)
                        }
                        onMouseOut={(e) =>
                            ((e.target as HTMLElement).style.backgroundColor =
                                styles.paginationBtn.backgroundColor)
                        }
                    >
                        Next
                    </a>
                )}
            </div>
        </div>
    );
};

export default AllTags;
