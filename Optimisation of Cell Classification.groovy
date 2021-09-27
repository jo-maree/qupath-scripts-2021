/* OPTIMISATION OF CELL CLASSIFICATION PARAMETERS IN FLUORESCENT TISSUE
*       Dr Jo-Maree Courtney, University of Tasmania, 2021
*       QuPath-0.2.3
*
*/


import qupath.lib.objects.PathCellObject
import qupath.lib.objects.PathDetectionObject
import qupath.lib.gui.dialogs.Dialogs
import java.text.SimpleDateFormat

SimpleDateFormat timestamp = new SimpleDateFormat("yyMMdd HHmm");
Date now = new Date()
def stamp = timestamp.format(now)

def regions = ['Upper Cortex', 'Lower Cortex', 'DG', 'CA1/CA3', 'Thalamus', 'Hypothalamus']
def DAPIthresholds = [150, 150, 75, 75, 150, 150]

def channelChoice = Dialogs.showChoiceDialog("Channel Options",
        "Which channel do you want to optimise?",
        ["DsRed", "GFP"],
        "DsRed")
print "You chose $channelChoice"

def thresholdSteps = Dialogs.showInputDialog("$channelChoice Threshold", "How many thresholds do you want to try?", 3)
def thresholdStart = Dialogs.showInputDialog("$channelChoice Threshold", "Starting threshold?", 100)
def thresholdIncrement = Dialogs.showInputDialog("$channelChoice Threshold", "Threshold increment?", 50)

def dir = buildFilePath(PROJECT_BASE_DIR, 'Optimisation Results')
mkdirs(dir)

def filepath = dir + '\\' + channelChoice + ' Results ' + stamp + '.csv'
File csvFile = new File(filepath)
csvFile.createNewFile()

new File(filepath).withWriter { fw ->
    fw.writeLine("Image,Annotation,DAPI Threshold,Threshold,Total Cells,$channelChoice Cells")

    def project = getProject()
    for (entry in project.getImageList()) {
        def imageData = entry.readImageData()
        def hierarchy = imageData.getHierarchy()
        def annotations = hierarchy.getAnnotationObjects()
        def name = entry.getImageName()

        def detections = hierarchy.getDetectionObjects()
        hierarchy.removeObjects(detections, true)

        for (annotation in annotations) {
            def annotationROI = annotation.getROI()
            def annotationType = annotationROI.getRoiName()

            if (annotationType.startsWith('Rectangle')) {

                hierarchy.getSelectionModel().clearSelection()
                hierarchy.getSelectionModel().setSelectedObject(annotation)
                def region = annotation.getName()

                for (int th = 0; th < thresholdSteps; th++) {
                    print annotation.getName()
                    def threshold = thresholdStart + thresholdIncrement * th

                    def n = regions.findIndexOf {it == region}
                    def DAPIthreshold = DAPIthresholds[n]

                    runPlugin('qupath.imagej.detect.cells.PositiveCellDetection',
                            imageData,
                            '{"detectionImage": "DAPI",  ' +
                                    '"requestedPixelSizeMicrons": 0.5,  ' +
                                    '"backgroundRadiusMicrons": 8.0,  ' +   //== Set optimised DAPI radius
                                    '"medianRadiusMicrons": 0.0,  ' +
                                    '"sigmaMicrons": 1.5,  ' +    //============ Set optimised DAPI sigma
                                    '"minAreaMicrons": 10.0,  ' +
                                    '"maxAreaMicrons": 400.0,  ' +
                                    '"threshold": ' + DAPIthreshold + ',  ' +
                                    '"watershedPostProcess": true,  ' +
                                    '"cellExpansionMicrons": 2.0,  ' +
                                    '"includeNuclei": true,  ' +
                                    '"smoothBoundaries": true,  ' +
                                    '"makeMeasurements": true,  ' +
                                    '"thresholdCompartment": "Cell: ' + channelChoice + ' mean", ' +
                                    '"thresholdPositive1": ' + threshold + ',  ' +
                                    '"thresholdPositive2": 100.0,  ' +
                                    '"thresholdPositive3": 100.0,  ' +
                                    '"singleThreshold": true}');


                    def objects = hierarchy.getObjectsForROI(null, annotation.getROI()).findAll {
                        it.isDetection()
                    }
                    hierarchy.getObjectsForROI(PathDetectionObject, annotation.getROI())
                    def detectionCount = objects.size()
                    def positiveCells = objects.findAll{
                        it.getPathClass() == getPathClass("Positive")
                    }

                    def data = name + ',' +
                            region + ',' +
                            Double.toString(DAPIthreshold) + ',' +
                            Double.toString(threshold) + ',' +
                            Double.toString(detectionCount) + ',' +
                            Double.toString(positiveCells.size())
                    fw.writeLine(data)

                }

            }
        }
        entry.saveImageData(imageData)
    }
}
print 'DONE!'

Dialogs.showPlainMessage('Optimisation', "Optimisation results saved.")

