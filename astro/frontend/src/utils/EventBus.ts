export const EventBus = {
    dispatch(event: string, detail?: any) {
        const customEvent = new CustomEvent(event, {
            detail,
            bubbles: true,
            composed: true
        });
        document.dispatchEvent(customEvent);
    },

    listen(event: string, callback: (e: CustomEvent) => void) {
        const handler = (e: Event) => {
            callback(e as CustomEvent);
        };

        document.addEventListener(event, handler);

        // Return an unsubscribe function
        return () => {
            document.removeEventListener(event, handler);
        };
    }
};