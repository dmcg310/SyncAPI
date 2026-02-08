import {METHOD_COLORS} from "../util/constants.ts";

export const getMethodColorClass = (method: string) => {
    return METHOD_COLORS[method as keyof typeof METHOD_COLORS] || 'bg-neutral-500';
};
