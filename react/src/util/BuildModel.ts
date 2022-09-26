import { MenuItem } from "primereact";
import { baseUrl } from "../RequestService";
import { HelpItem, HelpItemRoot } from "../Types";
import { concatClassnames } from "./ConcatClassNames";

/**
 * Returns the current item-model so it can be used by a PrimeReact menu
 * @param rawItems - an array of the help-items
 * @param currentModelState - the current model-state
 */
export function buildModel(rawItems: Array<HelpItem | HelpItemRoot>, currentModelState: MenuItem[], setUrlCallback: (url: string | undefined) => void): MenuItem[] {
    const modelMap: Map<string, number> = new Map<string, number>();

    const primeMenu = [...currentModelState];

    /**
     * Typescript type-identifier to check if the help-item is a root-item or not
     * @param item - the help-item to check
     */
    const itemIsNotRootItem = (item: HelpItem | HelpItemRoot): item is HelpItem => {
        return (item as HelpItem).parentID !== undefined;
    }

    /**
     * Returns the path to the help-item in the menu as array, to find it in the menu
     * @param id - the id of the item
     * @param parentID - the parent id of the item
     */
    const getPathToItem = (id: number | undefined, parentID: number) => {
        const pathArray = [];

        // Go from search-item-level to top level
        while (id !== -1) {
            if (id === undefined) {
                id = parentID;
            }
            else {
                id = modelMap.get(id.toString()) as number;
            }

            if (id !== -1) {
                pathArray.push(id)
            }
        }
        // reverse it because top-level needs to be first
        return pathArray.reverse();
    }

    rawItems.forEach(rawItem => {
        if (itemIsNotRootItem(rawItem)) {
            if (rawItem.id) {
                modelMap.set(rawItem.id.toString(), rawItem.parentID);
            }

            const path = getPathToItem(rawItem.id, rawItem.parentID);

            const menuItem: MenuItem = {
                id: rawItem.id?.toString() || undefined,
                label: rawItem.name,
                icon: rawItem.icon,
                style: rawItem.icon ? {
                    '--iconImage': 'url(' + baseUrl + rawItem.icon + ')',
                } : undefined,
                items: rawItem.id ? [] : undefined,
                className: concatClassnames(
                    rawItem.url ? `${rawItem.url} item-has-url` : "",
                    rawItem.icon ? "custom-menu-icon" : ""
                ),
                command: () => setUrlCallback(rawItem.url)
            }

            // If there is a path, go through it to find the correct spot for the help-item, else just add it to root
            if (path.length) {
                let menuIterator: MenuItem[] | undefined = primeMenu;

                for (let i = 0; i < path.length; i++) {
                    if (menuIterator) {
                        menuIterator = menuIterator.find(menuItem => menuItem.id === path[i].toString())?.items as MenuItem[] | undefined;
                    }
                }
                menuIterator?.push(menuItem)
            }
            else {
                primeMenu.push(menuItem);
            }
        }
        else {
            modelMap.set("-1", -1);
        }
    })

    return primeMenu
}