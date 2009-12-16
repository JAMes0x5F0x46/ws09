/* $Header: /var/lib/cvs/somtoolbox.bak/doc/scripts.js,v 1.3 2009-11-04 13:42:13 mayer Exp $ */

/* Takes cvs $_Id or $_Header tag and breaks it down to basic parts */
function getPageInfo(cvsID) {
	//trim it
	cvsID=cvsID.substring(cvsID.indexOf(": ")+2,cvsID.lastIndexOf(" $"));
	//split it at spaces
	var cvsIds=cvsID.split(" ");
	//take needed parts
	var filename=cvsIds[0].substring(0,cvsIds[0].indexOf(","));
	var revision=cvsIds[1];
	var date=cvsIds[2].replace("/","-").replace("/","-");
	var time=cvsIds[3];
	var author=cvsIds[4];
	if (author == 'mayer') {
	  author = 'Rudolf Mayer';
	} else if (author == 'lidy') {
	  author = 'Thomas Lidy';
	} else if (author == 'frank') {
      author = 'Jakob Frank';
	} 
	//return(''+filename+', revision '+revision+', last modified on '+date+' at '+time+' by '+author);
	return('Last modified on ' + date + ' by ' + author);
}

