/* TISSUE DETECTION
*       Dr Jo-Maree Courtney, University of Tasmania, 2021
*       QuPath-0.2.3
*
*/

import qupath.opencv.ml.pixel.OpenCVPixelClassifier

// =========== Set Channels and Colours =====================================================

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

// =========== Tissue and Vessel Detection =================================================
setImageType('FLUORESCENCE');
resetSelection();

def TissueDetection = """
{
  "pixel_classifier_type": "OpenCVPixelClassifier",
  "metadata": {
    "inputPadding": 0,
    "inputResolution": {
      "pixelWidth": {
        "value": 0.6412436675604641,
        "unit": "µm"
      },
      "pixelHeight": {
        "value": 0.6412407653462664,
        "unit": "µm"
      },
      "zSpacing": {
        "value": 1.0,
        "unit": "z-slice"
      },
      "timeUnit": "SECONDS",
      "timepoints": []
    },
    "inputWidth": 512,
    "inputHeight": 512,
    "inputNumChannels": 3,
    "outputType": "CLASSIFICATION",
    "outputChannels": [],
    "classificationLabels": {
      "1": {
        "name": "Tissue",
        "colorRGB": -154
      }
    }
  },
  "op": {
    "type": "data.op.channels",
    "colorTransforms": [
      {
        "combineType": "MEAN"
      }
    ],
    "op": {
      "type": "op.core.sequential",
      "ops": [
        {
          "type": "op.gaussian",
          "sigmaX": 2.0,
          "sigmaY": 2.0
        },
        {
          "type": "op.constant",
          "thresholds": [
            50.0
          ]
        }
      ]
    }
  }
}
"""
def tissueClassifier = GsonTools.getInstance().fromJson(TissueDetection, OpenCVPixelClassifier)

def VesselDetection = """
{
  "pixel_classifier_type": "OpenCVPixelClassifier",
  "metadata": {
    "inputPadding": 0,
    "inputResolution": {
      "pixelWidth": {
        "value": 0.6412436675604641,
        "unit": "µm"
      },
      "pixelHeight": {
        "value": 0.6412407653462664,
        "unit": "µm"
      },
      "zSpacing": {
        "value": 1.0,
        "unit": "z-slice"
      },
      "timeUnit": "SECONDS",
      "timepoints": []
    },
    "inputWidth": 512,
    "inputHeight": 512,
    "inputNumChannels": 3,
    "outputType": "CLASSIFICATION",
    "outputChannels": [],
    "classificationLabels": {
      "1": {
        "name": "Vessels",
        "colorRGB": -26215
      }
    }
  },
  "op": {
    "type": "data.op.channels",
    "colorTransforms": [
      {
        "channelName": "DsRed"
      }
    ],
    "op": {
      "type": "op.core.sequential",
      "ops": [
        {
          "type": "op.gaussian",
          "sigmaX": 2.0,
          "sigmaY": 2.0
        },
        {
          "type": "op.constant",
          "thresholds": [
            400.0
          ]
        }
      ]
    }
  }
}
"""
def vesselClassifier = GsonTools.getInstance().fromJson(VesselDetection, OpenCVPixelClassifier)

createAnnotationsFromPixelClassifier(tissueClassifier, 1000000.0, 1000.0, "SELECT_NEW")
runPlugin('qupath.lib.plugins.objects.DilateAnnotationPlugin', '{"radiusMicrons": -40.0,  "lineCap": "Round",  "removeInterior": false,  "constrainToParent": true}');
clearSelectedObjects(true);
clearSelectedObjects();
runPlugin('qupath.lib.plugins.objects.RefineAnnotationsPlugin', '{"minFragmentSizeMicrons": 10000.0,  "maxHoleSizeMicrons": 1000.0}');
resetSelection();
createAnnotationsFromPixelClassifier(vesselClassifier, 150.0, 1000.0, "SELECT_NEW")

// =========== Subtraction of Vessels from Tissue ================================================
def tissue = getAnnotationObjects().find {it.getPathClass() == getPathClass("Tissue")}
def vessel = getAnnotationObjects().find {it.getPathClass() == getPathClass("Vessels")}
def plane = tissue.getROI().getImagePlane()
if (plane != vessel.getROI().getImagePlane()) {
    println 'Annotations are on different planes!'
    return
}

// Convert to geometries & compute distance
// Note: see https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#distance-org.locationtech.jts.geom.Geometry-
def g1 = tissue.getROI().getGeometry()
def g2 = vessel.getROI().getGeometry()

def difference = g1.difference(g2)
if (difference.isEmpty())
    println "No intersection between areas"
else {
    def roi = GeometryTools.geometryToROI(difference, plane)
    def annotation = PathObjects.createAnnotationObject(roi, getPathClass('Tissue'))
    addObject(annotation)
    annotation.setName("Parenchyma")
    selectObjects(annotation)
    println "Annotation created for subtraction"
}
removeObject(tissue, true)
removeObject(vessel, true)

print 'Done!'
