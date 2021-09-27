/* From https://forum.image.sc/t/how-do-i-send-my-annotated-qupath-files-to-another-qupath-user-the-easiest-way/42979
* Script from Melvin Gelbard
* This script imports annotations from a file saved out by the 'Save Annotations' script
* */

import com.google.gson.reflect.TypeToken
import qupath.lib.io.GsonTools
import qupath.lib.objects.PathObject

// Instantiate tools
def gson=GsonTools.getInstance(true)

// Prepare template
def type = new TypeToken<List<PathObject>>() {}.getType()
def json_fp = promptForFile(null)

// Deserialize
deserializedAnnotations = gson.fromJson(json_fp.getText('UTF-8'), type)

// Add to image
addObjects(deserializedAnnotations)

// Resolve hierarchy
resolveHierarchy()

print "Done!"
