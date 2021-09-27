
/*From https://forum.image.sc/t/how-do-i-send-my-annotated-qupath-files-to-another-qupath-user-the-easiest-way/42979
* Base script from Melvin Gelbard
* [Author Information], 2021
* Note: this script has problems if the annotations have (or ever had) classes assigned.
* Export annotations like this:
*
* */


def annotations = getAnnotationObjects()
boolean prettyPrint = true
def gson = GsonTools.getInstance(prettyPrint)

def path = System.getProperty("user.home") + "\\Desktop\\annotations.json"
def file = new File(path)
file.write(gson.toJson(annotations))

print "Annotation file saved to Desktop"
