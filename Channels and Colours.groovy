// =========== Set Channels and Colours =====================================================
// This sets the channels to the correct colours (which for .vsi files corresponds to the
// order in which they were scanned) and assigns appropriate channel names.

setChannelColors(
        getColorRGB(0, 0, 255), // Blue
        getColorRGB(0, 255, 0), // Green
        getColorRGB(255, 0, 0), // Red
)

setChannelNames(
        'DAPI',
        'GFP',
        'DsRed',
)
