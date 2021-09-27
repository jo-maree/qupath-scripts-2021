/* CREATE AN INTERSECTION OF ROIS
*       Dr Jo-Maree Courtney, University of Tasmania, 2021
*       QuPath-0.2.3
*
*/

resetSelection();
def parenchyma = getAnnotationObjects().find {it.getName() == "Parenchyma"}
def parenchymaGeo = parenchyma.getROI().getGeometry()
def plane = parenchyma.getROI().getImagePlane()

def annotations = getAnnotationObjects()

for (annotation in annotations) {
    def annoName = annotation.getName()
    if (annoName != "Parenchyma") {
        print annoName
        def annoGeo = annotation.getROI().getGeometry()
        def intersect = annoGeo.intersection(parenchymaGeo)
        if (intersect.isEmpty())
            println "No intersection between areas"
        else {
            def roi = GeometryTools.geometryToROI(intersect, plane)
            def newAnno = PathObjects.createAnnotationObject(roi, getPathClass('Tissue'))
            addObject(newAnno)
            newAnno.setName(annoName)
            removeObject(annotation, true)
            print "Annotation created for intersect"
        }
    }
}
