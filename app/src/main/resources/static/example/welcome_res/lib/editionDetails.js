//////////////// Edition Details

  var col;
  
  var eoMsgs = {
	  notEnabled:"Optional and not enabled in your license",
	  disabledByConf:"Optional and enabled in your license but disabled by your configuration file",
	  enabled:"Optional and enabled in your license"
  };
  
  var eMsgs = {
	  notEnabled:"Not enabled in your license",
	  disabledByConf:"Enabled in your license but disabled by your configuration file",
	  enabled:"Enabled in your license"
  }

  function go() {
	//require(["js/localClient.js"], function(clientEdition) {
	require(["welcome_res/lib/clientEdition"], function(clientEdition) {
		
		clientEdition.disconnect();
		
		document.getElementById("loginerror").innerHTML = "";
		
		if ( (document.getElementById("user_auth").value.trim() != "") && (document.getElementById("user_pwd").value.trim() != "") ) {
			clientEdition.connectionDetails.setUser(document.getElementById("user_auth").value);
			clientEdition.connectionDetails.setPassword(document.getElementById("user_pwd").value.trim());
			
			clientEdition.connect();
		} else {
			alert("User or password should not be blank.");
		}
	});
  }

  require(["welcome_res/lib/clientEdition","Subscription","DynaGrid"], function(clientEdition,Subscription,DynaGrid) {
		var editionSubscription = new Subscription("MERGE", "monitor_identification", ["EDITION", "LICENSE_TYPE"]);
	
		editionSubscription.setRequestedSnapshot("yes");
		editionSubscription.addListener({
	
			onSubscription: function() {
				// console.log("OK.");
			},
		
			onItemUpdate: function(updInfo) {
				
				if ( updInfo.isValueChanged("EDITION") ) {
					var elems;
					if (updInfo.getValue("EDITION") == "COMMUNITY") {
						elems = document.getElementsByClassName("col2");
										
						for(var ind = 0; ind < elems.length; ++ind)	{
							elems[ind].style.color = "#333"
						}
					}
				}
				
				if ( updInfo.isValueChanged("LICENSE_TYPE") ) {
					var elems;
					
					if ( col ) {
						
						console.log("Reload!!!");
						window.location.reload(true);
						
					} else {
					
						if (updInfo.getValue("LICENSE_TYPE") == "FREE") {
							elems = document.getElementsByClassName("col2");
							col = "col2";
						} else if (updInfo.getValue("LICENSE_TYPE") == "DEMO") {
							elems = document.getElementsByClassName("col5");
							col = "col5";
						} else if (updInfo.getValue("LICENSE_TYPE") == "EVALUATION") {
							elems = document.getElementsByClassName("col6");
							col = "col6";
						} else if (updInfo.getValue("LICENSE_TYPE") == "STARTUP") {
							elems = document.getElementsByClassName("col7");
							col = "col7";
						} else if (updInfo.getValue("LICENSE_TYPE") == "NON-PRODUCTION-LIMITED") {
							elems = document.getElementsByClassName("col8");
							col = "col8";
						} else if (updInfo.getValue("LICENSE_TYPE") == "NON-PRODUCTION-FULL") {
							elems = document.getElementsByClassName("col9");
							col = "col9";
						} else if (updInfo.getValue("LICENSE_TYPE") == "PRODUCTION") {
							elems = document.getElementsByClassName("col10");
							col = "col10";
						} else if (updInfo.getValue("LICENSE_TYPE") == "HOT-STANDBY") {
							elems = document.getElementsByClassName("col11");
							col = "col11";
						}
						
						for(var ind = 0; ind < elems.length; ++ind)	{
							elems[ind].style.color = "#689A68";
							elems[ind].style.fontFamily = "'oxygenbold', sans-serif";
							
							if ( !(elems[ind].className.indexOf("h1") > -1) ) {
								//elems[ind].style.backgroundColor  = "#B6CF87";
								//elems[ind].style.backgroundColor  = "#FFF";
							
								elems[ind].style.backgroundColor = "#ECE981";	
							} else {
								elems[ind].style.color = "#ECE981";
							}
						}
						
						{
						
						var detailsSubscription = new Subscription("MERGE", "monitor_details", ["MAX_SSNS", "IS_MPN", "EXP_DATE", "MAX_RATE", "BAND", "TLS", "JMX", "VAL_TYPE"]);
						console.log("start subscribing monitor_details.");	
						//detailsSubscription.setItemGroup("monitor_details");
						//detailsSubscription.setFieldSchema("Detail_List");
					
						detailsSubscription.setRequestedSnapshot("yes");
						detailsSubscription.addListener({
					
							onSubscription: function() {
								console.log("monitor_details subscribed.");
							},
						
							onItemUpdate: function(updInfo) {
								
								if ( updInfo.isValueChanged("MAX_SSNS") ) {
									var elems;
									var ssns = updInfo.getValue("MAX_SSNS");
									
									elems = document.getElementsByClassName(col + " maxssns");
									for(var ind = 0; ind < elems.length; ++ind)	{
										if ( ssns == "0" ) {
											if ( (col == "col10") || (col == "col11") ) {
												elems[ind].innerHTML = "<p><abbr class='tooltip' title='Unlimited if you have a Per Server license; contractually limited if you have a Per Client or Per Limited Server license'>Unlimited<a href='#astk'>*</a> or contractually limited</abbr></p>";
											}
										} else {
											elems[ind].innerHTML = ""+ssns+"<br>as per license limitation";
										}
									}
								}
								if ( updInfo.isValueChanged("IS_MPN") ) {
									var elems;
									var mpns = updInfo.getValue("IS_MPN");
									
									updOptionalFeature("mpn", mpns);
								}
								
								if ( updInfo.isValueChanged("EXP_DATE") ) {
									var elems;
									var exp = updInfo.getValue("EXP_DATE");
									
									elems = document.getElementsByClassName(col + " expdate");
									for(var ind = 0; ind < elems.length; ++ind)	{
										if ( !exp || exp == "null") {
											if ( (col == "col8") || (col == "col9") || (col == "col10") || (col == "col11") ) {
												elems[ind].innerHTML = "<p><abbr class='tooltip' title='Never (for perpetual licenses) or after 1 year (for annual licenses)'>Never</abbr></p>";
											} else {
												elems[ind].innerHTML = "Never";
											}
										} else {
											elems[ind].style.color = "#F00";
											elems[ind].innerHTML = "<p><abbr class='tooltip' title='Never (for perpetual licenses) or after 1 year (for annual licenses)'>" + new Date(exp).toLocaleDateString() + "</abbr></p>";
										}
									}
								}
								
								if ( updInfo.isValueChanged("MAX_RATE") ) {
									var elems;
									var color;
									var rate = updInfo.getValue("MAX_RATE");
									
									elems = document.getElementsByClassName(col + " maxrate");
									for(var ind = 0; ind < elems.length; ++ind)	{
									
										if ( rate.substr(0,2) == "00" ) {
											color = "#FF6A00";
										} else {
											color = "#689A68";
										}
										
										if ( !rate || rate == "null") {
											// Should not happen.
										} else if (rate == 1 ) {
											elems[ind].style.color = color;
											if ( color === "#FF6A00" ) {
												elems[ind].innerHTML = "<p><abbr class='tooltip' title='1 message per second for each item of each client, reduced by your configuration file'>1</abbr></p>";
											} else {
												elems[ind].innerHTML = "<p><abbr class='tooltip' title='1 message per second for each item of each client'>1</abbr></p>";
												}
										} else if (rate > 1 ) {
											elems[ind].style.color = color;
											if ( color === "#FF6A00" ) {
												elems[ind].innerHTML = "<p><abbr class='tooltip' title='" + rate.substring(2, 3) + " messages per second for each item of each client, reduced by your configuration file'>" + rate.substring(2, 3) + "</abbr></p>";
											} else {
												elems[ind].innerHTML = "<p><abbr class='tooltip' title='" + rate.substring(0, 1) + " message per second for each item of each client'>" + rate.substring(0, 1) + "</abbr></p>";
											}
										} else {
											elems[ind].style.color = color;
											elems[ind].innerHTML = "<p><abbr class='tooltip' title='unlimited messages per second for each item of each client'>Unlimited<a href='#astk'>*</a></abbr></p>";
										}
									}
								}
								
								if ( updInfo.isValueChanged("BAND") ) {
									var elems;
									var band = updInfo.getValue("BAND");
									
									updOptionalFeature("band", band);
								}
								
								if ( updInfo.isValueChanged("TLS") ) {
									var elems;
									var tls = updInfo.getValue("TLS");
									
									updOptionalFeature("tls", tls);
								}
								
								if ( updInfo.isValueChanged("JMX") ) {
									var elems;
									var jmx = updInfo.getValue("JMX");
									
									updOptionalFeature("jmx", jmx);
								}
								
								if ( updInfo.isValueChanged("VAL_TYPE") ) {
									var elems;
									var val = updInfo.getValue("VAL_TYPE");
									
									elems = document.getElementsByClassName(col + " validation");
									for(var ind = 0; ind < elems.length; ++ind)	{
										if ( !val || (val == "null") || (val == "None") ) {
											elems[ind].style.color = "#689A68";
											elems[ind].innerHTML = "None";
										} else {
											elems[ind].style.color = "#689A68";
											elems[ind].innerHTML = "<p><abbr class='tooltip' title='Online or License File bound to the MAC address'>" + val + "</abbr></p>";
										}
									}
								}
								
							}
							
						});
						
						clientEdition.subscribe(detailsSubscription);
						
						var clientlibsSubscription = new Subscription("COMMAND", "monitor_client_libs", ["key", "command", "lib_ext", "free", "demo", "eval", "startup", "noprod-lim", "noprod-full", "prod", "standby", "ordinal"]);
						console.log("start subscribing monitor_client_libs.");
						clientlibsSubscription.setRequestedSnapshot("yes");
						
						var libsGrid = new DynaGrid("client_libs",false);
						libsGrid.setNodeTypes(["div", "abbr", "span", "input"]);
						libsGrid.parseHtml();
						libsGrid.setHtmlInterpretationEnabled(true);
						libsGrid.setSort("ordinal", false, true);
						
  						libsGrid.setAutoCleanBehavior(true,false);
						
						clientlibsSubscription.addListener(libsGrid);
						clientlibsSubscription.addListener({
							onSubscription: function() {
								console.log("monitor_client_libs subscribed.");
							}
						});
						
						libsGrid.addListener({
							onVisualUpdate: function(key,info,domNode) {
        						if (info == null) {
          							return;
        						}
								
								info.forEachChangedField(function changedFieldCallback(field, value){
									
									console.log("Changed: " + key + " -- " + field + ", " + value);
										
									if (field != "lib_ext") {
										if ( (field != "command") && (field != "key") && (field != "ordinal")  ) {
											var elems = domNode.getElementsByClassName(col);
											
											updCellClientLibs(elems, value);
											
										}
									} else {
										domNode.childNodes[1].innerHTML = "<p><abbr class='tooltip' title='" + getTooltipMessage(key.split(" ")[1]) + "'>" + value + "</abbr></p>";
									}
								});
							}
						});
					
						clientEdition.subscribe(clientlibsSubscription);
						
						return detailsSubscription;
						}
					}
				}
			}
		});
		
		clientEdition.subscribe(editionSubscription);
  });
  
//////////////// Utility functions

function fixFreeCol(jselems) {
	for(var ind = 0; ind < jselems.length; ++ind)	{
		jselems[ind].innerHTML = "<p><abbr class='tooltip_nou' title='" + eoMsgs["enabled"] + "'>&#10003;</abbr></p>";
	}
}

// Update edition details cell

function updCellClientLibs(elems, value) {
	for(var ind = 0; ind < elems.length; ++ind)	{
		if ( (col == "col8") || (col == "col9") || (col == "col10") || (col == "col11") ) {
			if ( value == "null" ) {
				elems[ind].style.color = "#E52424";
				elems[ind].innerHTML = "<p><abbr class='tooltip' title='" + eoMsgs["notEnabled"] + "'>&#10007;</abbr></p>";
			} else if ( value == "*" ) {
				elems[ind].style.color = "#689A68";
				elems[ind].innerHTML = "<p><abbr class='tooltip' title='" + eoMsgs["disabledByConf"] + "'>&#10007;</abbr></p>";
			} else {
				if ( value == "" ) {
					elems[ind].style.color = "#689A68";
					elems[ind].innerHTML = "<p><abbr class='tooltip' title='" + eoMsgs["enabled"] + "'><i>&#10003;</i><BR>any version</abbr></p>";
				} else {
					elems[ind].style.color = "#689A68";
					elems[ind].innerHTML = "<p><abbr class='tooltip' title='" + eoMsgs["enabled"] + "'><i>&#10003;</i><BR>up to v. "+value+"</abbr></p>";
				}
			}
		} else {
			if ( value == "null" ) {
				elems[ind].style.color = "#E52424";
				elems[ind].innerHTML = "<p><abbr class='tooltip' title='" + eMsgs["notEnabled"] + "'>&#10007;</abbr></p>";
			} else if ( value == "*" ) {
				elems[ind].style.color = "#689A68";
				elems[ind].innerHTML = "<p><abbr class='tooltip' title='" + eMsgs["disabledByConf"] + "'>&#10007;</abbr></p>";
			} else {
				if ( value == "" ) {
					elems[ind].style.color = "#689A68";
					elems[ind].innerHTML = "<p><abbr class='tooltip' title='" + eMsgs["enabled"] + "'><i>&#10003;</i><BR>any version</abbr></p>";
				} else {
					elems[ind].style.color = "#689A68";
					elems[ind].innerHTML = "<p><abbr class='tooltip' title='" + eMsgs["enabled"] + "'><i>&#10003;</i><BR>up to v. "+value+"</abbr></p>";
				}
			}
		}
	}
}

function updCellLib(line, value) {
	var elems = document.getElementsByClassName(col + " " + line);
	for(var ind = 0; ind < elems.length; ++ind)	{
		if ( (col == "col8") || (col == "col9") || (col == "col10") || (col == "col11") ) {
			if ( value == "null" ) {
				elems[ind].style.color = "#E52424";
				elems[ind].innerHTML = "<p><abbr class='tooltip' title='Optional and not enabled in your license'>&#10007;</abbr></p>";
			} else if ( value == "*" ) {
				elems[ind].style.color = "#689A68";
				elems[ind].innerHTML = "<p><abbr class='tooltip' title='Optional and enabled in your license but disabled by your configuration file'>&#10007;</abbr></p>";
			} else {
				if ( value == "" ) {
					elems[ind].style.color = "#689A68";
					elems[ind].innerHTML = "<p><abbr class='tooltip' title='Optional and enabled in your license'><i>&#10003;</i><BR></abbr></p>";
				} else {
					elems[ind].style.color = "#689A68";
					elems[ind].innerHTML = "<p><abbr class='tooltip' title='Optional and enabled in your license'><i>&#10003;</i><BR>up to v. "+value+"</abbr></p>";
				}
			}
		} else {
			if ( value == "null" ) {
				elems[ind].style.color = "#E52424";
				elems[ind].innerHTML = "<p><abbr class='tooltip' title='Not enabled in your license'>&#10007;</abbr></p>";
			} else if ( value == "*" ) {
				elems[ind].style.color = "#689A68";
				elems[ind].innerHTML = "<p><abbr class='tooltip' title='Enabled in your license but disabled by your configuration file'>&#10007;</abbr></p>";
			} else {
				if ( value == "" ) {
					elems[ind].style.color = "#689A68";
					elems[ind].innerHTML = "<p><abbr class='tooltip' title='Enabled in your license'><i>&#10003;</i><BR>any version</abbr></p>";
				} else {
					elems[ind].style.color = "#689A68";
					elems[ind].innerHTML = "<p><abbr class='tooltip' title='Enabled in your license'><i>&#10003;</i><BR>up to v. "+value+"</abbr></p>";
				}
			}
		}
	}
}

function updOptionalFeature(line, value) {
	var elems = document.getElementsByClassName(col + " " + line);
	for(var ind = 0; ind < elems.length; ++ind)	{
		if ( (col == "col8") || (col == "col9") || (col == "col10") || (col == "col11") ) {
			if ( !value || value == "null") {
				elems[ind].style.color = "#689A68";
				elems[ind].innerHTML = "<p><abbr class='tooltip' title='Optional and enabled in your license but disabled by your configuration file'>&#10007;</abbr></p>";
			} else if (value== "false" ) {
				elems[ind].style.color = "#E52424";
				elems[ind].innerHTML = "<p><abbr class='tooltip' title='Optional and not enabled in your license'>&#10007;</abbr></p>";
			} else {
				elems[ind].style.color = "#689A68";
				elems[ind].innerHTML = "<p><abbr class='tooltip' title='Optional and enabled in your license'><i>&#10003;</i></abbr></p>";
			}
		} else {
			if ( !value || value == "null") {
				elems[ind].style.color = "#689A68";
				elems[ind].innerHTML = "<p><abbr class='tooltip' title='Enabled in your license but disabled by your configuration file'>&#10007;</abbr></p>";
			} else if (value== "false" ) {
				elems[ind].style.color = "#E52424";
				elems[ind].innerHTML = "<p><abbr class='tooltip' title='Not enabled in your license'>&#10007;</abbr></p>";
			} else {
				elems[ind].style.color = "#689A68";
				elems[ind].innerHTML = "<p><abbr class='tooltip' title='Enabled in your license'><i>&#10003;</i></abbr></p>";
			}
		}
	}
}
