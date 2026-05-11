import type { SiteConfig } from "./types/config";
import { LinkPreset } from "./types/config";

const SITE_LANG = "en";
const SITE_TIMEZONE = 8;
export const siteConfig: SiteConfig = {
	title: "Mizuki",
	subtitle: "One demo website",
	siteURL: "https://mizuki.mysqil.com/",
	siteStartDate: "2025-01-01",
	timeZone: SITE_TIMEZONE,
	lang: SITE_LANG,
	themeColor: {
		hue: 240,
		fixed: false,
	},
	featurePages: {
		anime: true,
		diary: true,
		friends: true,
		projects: true,
		skills: true,
		timeline: true,
		albums: true,
		devices: true,
	},
	banner: {
		enable: false,
	},
};
