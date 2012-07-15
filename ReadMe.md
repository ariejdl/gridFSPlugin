# About
- Play 2.0 compatible Plugin for MongoDB's GridFS (chunked data storage).  Useful plugin if you're storing and serving user provided images.  Uses Play's Cache.scala as a template.
- Also contains some example code/helpers for:
	- serving 304 NotModified
	- uploading
	- retrieving
	- getting useful file attributes like the extension

### Simple Usage Notes
- Add src/GridFS.scala to your project
- your application.conf will need these settings

<pre>
mongo.gridfs.auth=0
mongo.gridfs.url="your_host"
mongo.gridfs.port=27017
mongo.gridfs.db="your_db"
</pre>

and for an authorised db (set mongo.gridfs.auth=1) the folowing extra:

<pre>
mongo.gridfs.username="your_uname"
mongo.gridfs.password="your_pwd"
</pre>


#### TODO
- Turn this quick code dump into an sbt project with a few tests
- Add a replace method

#### License
- Use in any project you please, no guarantees of any kind provided