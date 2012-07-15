package controllers

import play.api._
import play.api.mvc._

import org.joda.time.{LocalDate, DateTime, DateTimeZone}

object AController extends Controller {

def testFor304(params: Map[String, Seq[String]]): Boolean = {
  params.get(EXPIRES) match {
    case Some(values) => values.length == 1 match {
case true => core.utils.JodaT.httpTimeParse(values.head) match {
  case Some(date) => date.getMillis < new org.joda.time.DateTime().getMillis
  case _ => false
}
case _ => false
    }
    case _ => false
  }
}

/* e.g. a png, may want to check extension */
def getImage(id: String) = Action { implicit request => 

  testFor304(request.headers.toMap) match {
    case false => GridFS.get(id) match {
case Some(image) => Ok(core.utils.CoreGlobals.streamToBytes(image)).as("image/png").withHeaders(
  CACHE_CONTROL -> "max-age=260000", 
  PRAGMA -> "",
  EXPIRES -> core.utils.JodaT.httpTimePrint(new LocalDate().plusDays(5).toDateTimeAtStartOfDay),
  ETAG -> "xx"
)
case _ => Results.NotFound
    }
    case true => NotModified
  }

}

case class FileDetails(fileFull: MultipartFormData.FilePart[TemporaryFile], file: java.io.File, path: String, params: Map[String, Seq[String]], name: String, mime: Option[String], extension: Option[String])
def wrapFileUpload(exec: FileDetails => Result)(implicit request: Request[MultipartFormData[TemporaryFile]]): Result = {
    request.body.file("files[]").map{ file =>
val params = request.body.asFormUrlEncoded
      val name = file.filename
val extension = name.split('.').lastOption
exec(FileDetails(file, file.ref.file, file.ref.file.getAbsolutePath, params, name, file.contentType, extension))
    }.getOrElse(Results.NotFound)
 }


def fileUpload = Action(parse.multipartFormData) { implicit request => wrapFileUpload { fileObj =>
		
	/* may want to check the extension, filesize etc. */
	
	val imageName = "e.g. a UUID"
	GridFS.setFile(imageName, fileObj.path)
	
	Ok("saved")

}

}

}