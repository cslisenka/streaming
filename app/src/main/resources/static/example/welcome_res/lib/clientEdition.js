  var first_attempt = true;
  
  define(["LightstreamerClient","StatusWidget"],function(LightstreamerClient,StatusWidget) {
    var localClient = new LightstreamerClient();

    localClient.connectionDetails.setServerAddress("http://localhost:8080");
	localClient.connectionDetails.setAdapterSet("MONITOR");
	localClient.connectionOptions.setMaxBandwidth(100);
	// localClient.connectionOptions.setServerInstanceAddressIgnored(true);
	
	localClient.connectionDetails.setUser("z");
	localClient.connectionDetails.setPassword("z");
	
	localClient.connectionSharing.enableSharing("MonitorEngine","IGNORE","CREATE",true);  
	
	localClient.addListener({
		onServerError: function(errorCode, errorMessage) {
			
			if (errorCode == 1) {
				setTimeout(function(){ 
					document.getElementById("request_auth").style.display = "";
					document.getElementById("request_auth").style.top = "-2090px";
					if (!first_attempt) {
						document.getElementById("loginerror").innerHTML = "Login failed.";
						
					} else {
						first_attempt = false;
					}
				}, 800);
			}
			console.log("ERRORE: " + errorCode + ", " + errorMessage);
	
		}, 
		 onStatusChange: function(chngStatus) {
			if (chngStatus.indexOf("CONNECTED") == 0) {
				document.getElementById("request_auth").style.display = "none";
				document.getElementById("request_auth").style.top = "+2590px";
			}
		}
	});
	
	localClient.connect();
	return localClient;
  });
