import {SERVER_URI} from "../utils/Constants.ts";
import Cookies from "js-cookie";

export async function getTagCountOfUser(): Promise<number | null> {
    const token = Cookies.get("token");
    if (!token) {
        console.log("No Token found in cookie");
        return null;
    }

    const url = `${SERVER_URI}/authenticated/tag/count`;
    const response = await fetch(url, {
        method: "GET",
        headers: {"content-type": "application/json", "Authorization": `Bearer ${token}`},
    });
    const json = await response.json();
    return parseInt(json["data"]);
}