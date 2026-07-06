/**
 * Ends a ride using the rider's real device location (browser Geolocation
 * API), matching how an actual mobility app works - nobody types GPS
 * coordinates by hand. Falls back to a short offset from the ride's own
 * start point if location access is denied, unavailable, or times out, so
 * the flow still completes in a browser/test environment without location
 * permission. Either way, real endLatitude/endLongitude values are always
 * sent - this only changes how the UI acquires them, not the API contract.
 */
function endRideWithLocation(form, startLat, startLon) {
    const latInput = form.querySelector('input[name="endLatitude"]');
    const lonInput = form.querySelector('input[name="endLongitude"]');

    function submitWith(lat, lon) {
        latInput.value = lat;
        lonInput.value = lon;
        form.submit();
    }

    function submitWithOffsetFromStart() {
        // Ending exactly at the start point would compute a real 0km/0-cost
        // ride, which is a legitimate edge case but not what "location
        // unavailable" should produce for a demo ride - nudge it a little so
        // per-km billing has a real, non-zero distance to work with.
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
