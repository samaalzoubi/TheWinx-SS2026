// we use the browser's real geolocation here so nobody has to type GPS coordinates by hand, falling back to an offset from the start point if location isn't available
function endRideWithLocation(form, startLat, startLon) {
    const latInput = form.querySelector('input[name="endLatitude"]');
    const lonInput = form.querySelector('input[name="endLongitude"]');

    function submitWith(lat, lon) {
        latInput.value = lat;
        lonInput.value = lon;
        form.submit();
    }

    function submitWithOffsetFromStart() {
        // we nudge the coordinates here so a demo ride gets a real non-zero distance instead of landing exactly on the start point
        submitWith(startLat + 0.006, startLon + 0.004);
    }

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            pos => submitWith(pos.coords.latitude, pos.coords.longitude),
            submitWithOffsetFromStart,
            { timeout: 4000 }
        );
    } else {
        submitWithOffsetFromStart();
    }
    return false;
}
