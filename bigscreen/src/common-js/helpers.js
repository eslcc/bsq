export function relativePathToAbsolute(pathI) {
    let path = pathI;
    if (path.indexOf("/") === -1) {
        path = "/" + path;
    }
    if (process.env.NODE_ENV === 'development') {
        return `http://localhost:8080${path}`;
    } else {
        return `http://${window.location.host}${path}`;
    }
}

export function getKeyByValue(object, value) {
    for (var prop in object) {
        if (object.hasOwnProperty(prop)) {
            if (object[prop] === value)
                return prop;
        }
    }
}
