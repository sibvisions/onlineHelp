export function concatClassnames(...classNames: (string | null | undefined)[]) {
    return classNames.filter(Boolean).join(' ');
}