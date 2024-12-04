export default interface Video {
    videoId: string
    title : string
    description: string
    thumbnailUrl: string
    tags: Set<string>
}