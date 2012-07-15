package core
package plugins

import play.api._

/*
 https://github.com/playframework/Play20/blob/master/framework/src/play/src/main/scala/play/api/cache/Cache.scala
 plugin example
 https://github.com/playframework/Play20/wiki/Scalaplugin
*/
trait GridFSApi {
  def setFile(key: String, file: String)
  def set(key: String, bytes: Array[Byte])
  def get(key: String): Option[java.io.InputStream]
  def delete(key: String): Unit
}

abstract class GridFSPluginWrapper extends play.api.Plugin { def api: GridFSApi }

object GridFS {

 private def error = throw new Exception(
    "There is no cache plugin registered. Make sure at least one CachePlugin implementation is enabled."
  )

  def setFile(key: String, file: String)(implicit app: Application) = {
    app.plugin[GridFSPlugin].map(_.api.setFile(key, file)).getOrElse(error)
  }

  def set(key: String, value: Array[Byte])(implicit app: Application) = {
    app.plugin[GridFSPlugin].map(_.api.set(key, value)).getOrElse(error)
  }

  def get(key: String)(implicit app: Application): Option[java.io.InputStream] = {
    app.plugin[GridFSPlugin].map(_.api.get(key)).getOrElse(error)
  }

  def delete(key: String)(implicit app: Application): Unit = {
    app.plugin[GridFSPlugin].map(_.api.delete(key)).getOrElse(error)
  }


}

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.gridfs._;

class GridFSPlugin(app: Application) extends GridFSPluginWrapper {
  val config = app.configuration

  val m = new Mongo(config.getString("mongo.gridfs.url").getOrElse("localhost"),
		    config.getInt("mongo.gridfs.port").getOrElse(27017))
  
  val db = m.getDB(config.getString("mongo.gridfs.db").getOrElse("test"))

  (config.getInt("mongo.gridfs.auth").getOrElse(0) == 1) match {
    case true => {
      db.authenticate(config.getString("mongo.gridfs.username").getOrElse("fail"),
		      config.getString("mongo.gridfs.password").getOrElse("fail").toArray) match {
	case true => Unit
	case false => throw new IllegalArgumentException("mongodb GridFS auth fail")
      }
    }
    case false => Unit
  }


  val gFS = new GridFS(db)

  override lazy val enabled = true
  
  override def onStart = {
    Logger.info("grid FS started up")
  }

  override def onStop = {
    m.close
  }

  lazy val api = new GridFSApi {
    
    def setFile(key: String, file: String) = {
      val toSave = gFS.createFile(new java.io.File(file))
      toSave.setFilename(key)
      toSave.save
    }

    // TODO: need a replace method
    def set(key: String, bytes: Array[Byte]) = {
				throw new IllegalArgumentException("Not implemented")
//      gFS.storeFile
    }

/*
 * http://groups.google.com/group/play-framework/browse_thread/thread/e7c4b91ec5ab82e5/ce44ae3e85ab0f86?lnk=gst&q=2.0+image+byte#ce44ae3e85ab0f86
 * byte array/stream
 */

    def get(key: String) = {
      gFS.findOne(key) match {
	case file: GridFSDBFile => {
	  val stream = file.getInputStream
	  Some(stream)
	}
	case _ => None
      }
    }

    def delete(key: String): Unit = gFS.remove(key)

  }


}

