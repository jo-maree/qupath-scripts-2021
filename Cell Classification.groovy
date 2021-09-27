/* CELL CLASSIFICATION
*       Dr Jo-Maree Courtney, University of Tasmania, 2021
*       QuPath-0.2.3
*
*  This script needs to be run for project once for each region.
*  After running each region, export annotation measurements but
*  only use the data for the most recent region as the other
*  DAPI thresholds will be wrong.
*
*/

import qupath.lib.gui.dialogs.Dialogs

def hierarchy = getCurrentHierarchy()

def regions = ['Cortex', 'Hippocampus', 'Thalamus', 'Hypothalamus']
def DAPIthresholds = [150, 75, 150, 150]
def classifiers = ["DsRed-GFP Cortex", 'DsRed-GFP Hippocampus', 'DsRed-GFP Thalamus', 'DsRed-GFP Hypothalamus']

def regionChoice = regions[2]

print "You chose $regionChoice"

def index = regions.indexOf(regionChoice)
def DAPIthreshold = DAPIthresholds[index]

def annotations = getAnnotationObjects()

for (annotation in annotations) {

    if (annotation.getName() == regionChoice) {
        hierarchy.getSelectionModel().clearSelection()
        hierarchy.getSelectionModel().setSelectedObject(annotation)

        runPlugin('qupath.imagej.detect.cells.WatershedCellDetection',
                '{"detectionImage": "DAPI",  ' +
                        '"requestedPixelSizeMicrons": 0.5,  ' +
                        '"backgroundRadiusMicrons": 8.0,  ' +
                        '"medianRadiusMicrons": 0.0,  ' +
                        '"sigmaMicrons": 1.5,  ' +
                        '"minAreaMicrons": 10.0,  ' +
                        '"maxAreaMicrons": 400.0,  ' +
                        '"threshold": ' + DAPIthreshold + ',  ' +
                        '"watershedPostProcess": true,  ' +
                        '"cellExpansionMicrons": 2.0,  ' +
                        '"includeNuclei": true,  ' +
                        '"smoothBoundaries": true,  ' +
                        '"makeMeasurements": true}');
    }
}

runObjectClassifier(classifiers[index])
detectionCentroidDistances(true)


