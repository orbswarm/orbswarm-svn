
var swarm = [];
swarm.SERVICES = true;

var swarmWS = {};
	swarmWS.URL = "/services";
    //swarmWS.serviceAccountURL = "/services/user/account";
    //swarmWS.serviceMessagingStationURL = "/services/messaging/station";
    //swarmWS.serviceMessagingUserURL = "/services/messaging/user";
	//swarmWS.serviceMessagingStationURL = "/php/getStationMessagesSimulator.php";
	//swarmWS.serviceNewMessagingStationURL = "/php/getNewStationMessagesSimulator.php";
	//swarmWS.serviceSubmitMessagingStationURL = "/php/submitStationMessageSimulator.php";
    //swarmWS.serviceSubmitMessagingStationURL = "/services/messaging/station";
    //swarmWS.serviceNewMessagingStationURL = "/services/messaging/station";
    //swarmWS.serviceContentRetrieveURL     = "/services/content/retrieve";
    //swarmWS.serviceContentUploadURL       = "/services/content/upload";
    //swarmWS.serviceContentStationURL       = "/services/content/station";
	//swarmWS.serviceContentPhotoURL		= "/services/user/upload/photo";
    //swarmWS.serviceVoteURL       = "/services/gaming/vote";
	//swarmWS.serviceRocketURL       = "/services/gaming/rocket";
	//swarmWS.serviceBombURL       = "/services/gaming/bomb";
    //swarmWS.serviceDeviceURL       = "/services/station/device";


// Common function used by all pages that wish to communicate with
// the Jelli web services. 
//
// Pass:
//
// package    : which web service package contains the action processor for the requested action.
// json-data  : data object (which minimally needs to contain the action property).
// successsCB : this callback is called on success and will be passed the result reponse object that 
//              comes back from the server.
// failureCB  : called when either the server fails or the Ajax call failes.  Any Ajax errors will be 
//              converted into a classic "unexpected error" as the client can't do anything different 
//              between Ajax or server errors (i.e. it's an 'oops! page.').
// 
swarmWS.serviceCall = function(thisPackage, json_data, successCB, failureCB) {

   //jelli.DEBUGGING = true;

   var url = "";
    
   // Map the package to it's corresponding service URL.
   //
   switch (thisPackage) {

     	case "swarm":
		   url = swarmWS.URL;
		break;

/*
		case "comments":
		   url = swarmWS.serviceMessagingStationURL;
		break;

		case "newcomments":
		   url = swarmWS.serviceNewMessagingStationURL;
		break;

		case "submitcomment":
		   url = swarmWS.serviceSubmitMessagingStationURL;
		break;

		case "retrieve":
		   url = swarmWS.serviceContentRetrieveURL;
		break;

		case "upload":
		   url = swarmWS.serviceContentUploadURL;
		break;

		case "messages":
			url = swarmWS.serviceMessagingUserURL;
		break;

		case "vote":
			url = swarmWS.serviceVoteURL;
		break;

		case "rocket":
			url = swarmWS.serviceRocketURL;
		break;
		
		case "game":
			url = swarmWS.serviceContentStationURL;
		break;

		case "bomb":
			url = swarmWS.serviceBombURL;
		break;

		case "notifications":
			url = swarmWS.serviceContentRetrieveURL;
		break;
		
		case "history":
			url = swarmWS.serviceContentStationURL;
		break;
*/		
		
	//	case "device":
	//		url =	swarmWS.serviceDeviceURL;
	//	break;

		default:
		//	alert("swarmWS: no URL found for package: " + package); // Should never happen!
		return;
   }


   json_data["valid"] = undefined; // Remove this temporary property...

   var json_text = JSON.stringify(json_data);

	if (swarm.SERVICES) {
	   trace("Request Object: " + json_text);

	   var xmr = $.ajax({
	      url     : url, 
	      data    : json_text,
	      type    : "POST",
	      dataType: "json",

	      success: function(data, stat) {
	         //trace("Ajax success callback: " + xmr.responseText);
	         if (data.result) successCB(data);
	         else             failureCB(data);
         
	        // jelli.DEBUGGING = true;
	      },
	      error: function(xhr, stat, err) {
	         trace("AJAX error callback: " + stat + " - response text : " + xhr.responseText);

	         var syntheticResponse = {result: false, reasonMap: {error:"unexpectedError"}};
	         failureCB(syntheticResponse);

	         //jelli.DEBUGGING = true;
	      }
	   });
	}
}
