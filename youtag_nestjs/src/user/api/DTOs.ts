export interface OAuthUserDTO {
  name: string;
  id: string; // Typically, this should be email from Google.
  thumbnailUrl?: string;
}

export interface UserDTO {
  name: string;
  id: string;
  thumbnailUrl: string;
}
