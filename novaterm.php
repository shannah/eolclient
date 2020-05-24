<?php
ini_set('memory_limit', '512M');
class Novaterm {
	var $username="SYSOP";
	var $password="flutie";
	var $nonce="1524961864";
	var $realm = "DemoCentral";
    
	
	function createDigestAuthHeader($uri, $ircType = false, $method='GET') {
        if ($ircType) {
            $method = 'irc';
        }
		/*
		HA1 = MD5(username:realm:password)
		HA2 = MD5(method:digestURI)
		response = MD5(HA1:nonce:HA2)
		*/
		$ha1 = md5($this->username.':'.$this->realm.':'.$this->password);
		$ha2 = md5($method.':'.$uri);
		$response = md5($ha1.':'.$this->nonce.':'.$ha2);
        if ($ircType) {
            
    		$ha1 = md5($this->username.':'.$this->realm.':'.$this->password);
            //echo "md5({$this->username}:{$this->realm}:{$this->password}) => $ha1 \n";
    		$ha2 = md5($method.':'.$uri);
            //$ha2 = md5($uri);
            //echo "md5($method:$uri) => $ha2\n";
    		$response = md5($ha1.':'.$this->nonce.':'.$ha2);
            //echo "md5($ha1:$this->nonce:$ha2) => $response\n";
            return 'AUTH :digest username="'.$this->username.'",realm="'.$this->realm.'",nonce="'.$this->nonce.'",uri="'.$uri.'",response="'.$response.'"';
        }
		return 'Authorization: digest username="'.$this->username.'",realm="'.$this->realm.'",nonce="'.$this->nonce.'",uri="'.$uri.'",response="'.$response.'"';
		
		

	}
	
    function login($ip, $username, $password) {
        $this->username = $username;
        $this->password = $password;
		$host = $ip;
		$port = 194;

        $fp = fsockopen($host, $port, $errno, $errstr, $timeout = 30);
		if(!$fp){
    		//error tell us
    		throw new Exception("$errstr ($errno)\n");
            
		}
        fputs($fp, "NONC :user=\"$username\"\r\n");
        $response = fgets($fp);
        if (trim($response)) {
            if (trim($response) == '464') {
                throw new Exception("Already logged in from this address.");
            }
            // e.g. 300 :digest realm="DemoCentral",nonce="487023769"
            $realm = null;
            $nonce = null;
            if (preg_match('#realm="(.*?)"#', $response, $matches)) {
                $realm = $matches[1];
            }
            if (preg_match('#nonce="(.*?)"#', $response, $matches)) {
                $nonce = $matches[1];
            }
            if ($realm and $nonce) {
                $this->realm = $realm;
                $this->nonce = $nonce;
                echo "Auth header = ".$this->createDigestAuthHeader('/', true)."\n";
                fputs($fp, $this->createDigestAuthHeader('/', true)."\r\n");
                $response = fgets($fp);
                if (trim($response) == '300') {
                    echo "Logged In\n";
                    echo "Waiting 60 seconds before logging out...";
                    sleep(30);
                    
                }
                echo "Response: $response\n";
                fclose($fp);
                return true;
                
            } else {
                fclose($fp);
                return false;
            }
            
        }
        fclose($fp);
        return false;
    }
	
	
	function get($ip, $url, $htmlOut = null, $binaryOut = null) {
		$url = str_replace(' ', '%20', $url);
		//echo $url;exit;
		if (!$htmlOut) {
			$htmlOut = STDOUT;
		}
		if (!$binaryOut) {
			$binaryOut = STDOUT;
		}
		
		$host = $ip;
		$port = 80;
		$path = $url;

		$passthru = preg_match('#^/file/#i', $url) or preg_match('#^http://\*/file/#i', $url);

		$fp = fsockopen($host, $port, $errno, $errstr, $timeout = 30);

		if(!$fp){
		//error tell us
		echo "$errstr ($errno)\n";
  
		}else{

            //echo  $this->createDigestAuthHeader($url);exit;
		  fputs($fp, "GET $url HTTP/V1.0\r\n");
		  fputs($fp, $this->createDigestAuthHeader($url)."\r\n");
		  fputs($fp, "Connection maintain\r\n");
		  fputs($fp, "User-Agent: ResNova_NovaTerm_Mac/4.0\r\n");

		  fputs($fp, "\r\n");
		  
		  
		  //fwrite($fp, $req);
		  
		  //loop through the response from the server
		  $inHeaders = true;
		  //$out = '';
		  $isHtml = false;
		  $htmlOutStr = '';
		  $boundary = null;
		  $multipart = false;
		  while(!feof($fp)) {
		  	
			$line = fgets($fp, 4096);
			//echo $line;
			$firstLine = true;
			if ($inHeaders) {
			
				if (!trim($line)) {
					$inHeaders = false;
				} else {
                    $key = trim(substr($line, 0, strpos($line, ':')));
                    $value = trim(substr($line, strpos($line, ':')+1));
                    if (strcasecmp($key, 'content-type') === 0 and strpos($value, 'multipart/mixed') === 0) {
                        //echo "multipart";
                        if (preg_match('#boundary="(.*?)"#', $value, $matches)) {
                            //echo "Matched";
                            //print_r($matches);
                            $boundary = $matches[1];
                        }
                        //echo "Boundary: $boundary";exit;
                    }
               
				}
				//echo $line;
			} else {
			    if ($boundary) {
			        if (trim($line) == '--'.$boundary) {
			            $inHeaders = true;
			            continue;
			        }
			        if (trim($line) == '--'.$boundary.'--') {
			            break;
			            
			        }
			    }
				if (!$passthru and $firstLine) {
					if (strlen($line) > 0) {
						$firstLine = false;
						
						if ($line{0} == '<' or $line{0} == '>') {
							$isHtml = true;
							$out = $htmlOut;
						} else {

							if (substr($line, 0, 3) == 'GIF') {
							//	//echo "here";
								header('Content-type:image/gif');
							}
							$out = $binaryOut;
						}
					}
				}
				if ($isHtml) {
					$htmlOutStr .= str_replace("\r", "", $line);
				} else {
					echo $line;
				}
				
			}
		  }
		  
		  
		  //close fp - we are done with it
		  fclose($fp);
		  
		  if ($passthru) {
		  	if ($isHtml) {
		  		echo $htmlOutStr;
		  	}
		  	exit;
		  }
		  if (!$passthru) {
	//echo "here";exit;
		  	$doc = @DOMDocument::loadHTML($htmlOutStr);
		  	$xpath = new DOMXPath($doc);
		  	//$figs = $xpath->query('//fig');
		  	$image = null;
		  	$width = 0;
		  	$height = 0;
		  	$map = "<map name='menu'>\n";
		  	$templateName = null;
		  	
		  	
		  	foreach ($xpath->query('//fig') as $fig) {
		  		$image = $fig->getAttribute('src');
		  		foreach ($xpath->query('a', $fig) as $a) {
		  			$href = $_SERVER['PHP_SELF'].'?uri='.$a->getAttribute('href');
					$shape = $a->getAttribute('shape');
					$shapeType = substr($shape, 0, strpos($shape, ' '));
					if ($shapeType == 'oval') {
						$shapeType = 'circle';
					}
					$coords = str_replace(' ', '', substr($shape, strpos($shape, ' ')+1));
					$coords = explode(',', $coords);
					if ($shapeType == 'rect') {
						$coords = [
							intval(trim($coords[0])),
							intval(trim($coords[1])),
							intval(trim($coords[0])) + intval(trim($coords[2])),
							intval(trim($coords[1])) + intval(trim($coords[3]))
						];
					} else {
						$coords = [
							intval(trim($coords[0])),
							intval(trim($coords[1])),
							min(intval(trim($coords[2])), intval(trim($coords[3])))
						];
					}
					
					$coords = implode(',', $coords);
					$map .= "<area shape='$shapeType' coords='$coords' href='$href'/>\n";
		  		}
		  	}
		  	
		  	$map .= "</map>\n";
		  	foreach ($xpath->query('//template') as $tpl) {
		  		$size = $tpl->getAttribute('size');
		  		list($width, $height) = explode(';', $size);
		  		$width = intval($width);
		  		$height = intval($height);
		  		$templateName = $tpl->getAttribute('name');
		  	}

		  	if ($templateName == 'NavigateWindow') {
		  		//echo "here";exit;
		  			//print_r($doc->saveHTML());
				$halfHeight = intval($height/2);
				$halfWidth = intval($width/2);
				$buf = <<<END
<!doctype html>
<html>
<head>
<meta name="viewport" content="initial-scale=1.0">
<meta name="viewport" content="width=480">
<style>
html, body {
	height: 100%;
	margin:0;
	padding:0;
}
body {
	position:relative;
}
.bgimg {

	background-image: url("{$_SERVER['PHP_SELF']}?uri=$image");
	background-position: center;
	background-repeat: no-repeat;
	background-size: cover;
	filter: blur(24px);
	-webkit-filter: blur(24px);
	height:100%;
	width: 100%;
}
.body {

	position:absolute;
	top: 50%;
	left: 50%;
	margin-left: -{$halfWidth}px;
	margin-top: -{$halfHeight}px;
	width: {$width}px;
	height: {$height}px;
	display:block;
}
</style>
</head>
<body style="width:100%; height:100%">
<div class="bgimg"></div>
<img class="body" src="{$_SERVER['PHP_SELF']}?uri=$image" width="$width" height="$height" usemap="#menu"/>
{$map}
</body>
</html>	  	
END;

				echo $buf;
		  	} else if ($templateName == 'NovaViewWindow') {
		  		include 'templates/NovaViewWindow.php';
		  		exit;
		  	} else if ($templateName == 'ListLibraryWindow') {
                include 'templates/ListLibraryWindow.php';
		  	    exit;
		  	}
		  	
		  
		  }
		  
		  else {
		 	//echo $htmlOutStr;
		 }
		  
		  
		}
		 
	}
}
/*
$out = <<<END
<html><template name="NavigateWindow" size="467;350"><title>Main Menu</title>
<part name="Navigate Edit part">
<fig src="/file/MainMenu" version="Sat, 22 Jul 1995 10:49:31 -0700">
<a shape="oval 294, 20, 20, 19" href="/design/837" effect=replace></a>
<a shape="oval 168, 300, 25, 23" href="/design/1447" effect=replace></a>
<a shape="oval 35, 300, 24, 23" href="quit://" effect=replace></a>
<a shape="rect 305, 289, 133, 30" href="/design/6727" effect=replace></a>
<a shape="rect 305, 224, 132, 31" href="/design/783" effect=replace></a>
<a shape="rect 305, 159, 132, 31" href="/design/729" effect=replace></a>
<a shape="rect 305, 95, 132, 31" href="/news/Product Info/" effect=new></a></fig></part></html>
END;

$out = str_replace("\r", "", $out);
echo $out;
exit;
*/
/*
<html><template name="NavigateWindow" size="467;350"><title>Main Menu</title>
<part name="Navigate Edit part">
<fig src="/file/MainMenu" version="Sat, 22 Jul 1995 10:49:31 -0700">
href="/design/837" effect=replace></a>
href="/design/1447" effect=replace></a>
href="quit://" effect=replace></a>
href="/design/6727" effect=replace></a>
href="/design/783" effect=replace></a>
href="/design/729" effect=replace></a>
href="/news/Product Info/" effect=new></a></fig></part></html>Connection closed by foreign host.
*/
$novaterm = new Novaterm();
//$novaterm->get('10.0.1.110', 'http://*/design/17');
//$novaterm->get('10.0.1.110', 'http://*/Design/837');
//$novaterm->get('10.0.1.110', '/news/Product%20Info');
//$novaterm->get('10.0.1.110', '/file/MainMenu');
//$novaterm->get('10.0.1.110', '/file/MainMenu');
//$novaterm->get('10.0.1.110', '/templates/NavigateWindow');
//$novaterm->get('10.0.1.110', '/library/Mac%20Software');
//$novaterm->get('10.0.1.110', '/library/Mac%20Software/2.48157519.DemoCentral');
//$novaterm->get('10.0.1.110', '/library/Mac%20Software/2.48157519.DemoCentral/');
if (@$argv) {
    if (count($argv) > 1 and $argv[1] == 'fuzz') {
    	//$novaterm->get('10.0.1.110', $argv[1]);
        //$novaterm->login('10.0.1.110', 'SYSOP', 'FLUTIE');
        $methods = array('300','CHALLENGE','ICMP','QUIT','NULL','null','NA','UNKNOWN','?','SYSOP','TCP','MODEM','ECHO','MD5','LOGIN','USERNAME','USER','PASSWORD','AUTHENTICATE','AUTHORIZE','*','DIGEST','LOGIN','JOIN','OPTIONS','GET','HEAD','POST','PUT','DELETE','CONNECT','TRACE','PATCH','NONC','JOIN','IRC','','*','AUTH','PASS','NICK','PING','PONG','VERSION','get', 'POST','TELNET','telnet','HTTP','http','DIGEST','digest','','RNP','NOVA','NT','/',' ', '-','AUTHENTICATE','COMMAND','ADMIN','INFO', 'PASS','PRIVMSG','SIGNIN','CONNECT','/','10.0.1.77') ;
    
        $words = array(); //file('/Users/shannah/Downloads/words.txt');
        $words = array_merge($methods, $words);
        $words[] = ':digest';
        $words[] = ':DIGEST';
        $words[] = '#';
        $words[] = 'NOVATERM';
        $words[] = 'NV';
        $words[] = 'NOVA';
        $words[] = 'NLP';
        $words[] = 'NS';
        $words[] = 'NOVASERVER';
        $words[] = 'FTP';
        $words[] = 'INTERACT';
        $words[] = 'WAIS';
        $words[] = 'PROSPERO';
        $words[] = 'AUTH';
        $words[] = 'AUTH NONC';
        $words[] = '^';
        $words[] = '????';
        $words[] = '???';
        $words[] = '?';
        $words[] = '??';
        $words[] = ':';
        $words[] = '.';
        $words[] = 'NT';
        $words[] = 'testirc';
        $words[] = 'NovaTerm';
    
        $uppercase = array();
        $lowercase = array();
        $ucfirst = array();
        foreach ($words as $w) {
            $uppercase[] = strtoupper($w);
            $lowercase[] = strtolower($w);
            $ucfirst[] = ucfirst(strtolower($w));
        }
        $words = array_merge($words, $uppercase, $lowercase, $ucfirst);
    
        foreach ($words as $method) {
            $method = trim($method);
            foreach (array('SYSOP') as $user) {
                $novaterm->username = $user;
                foreach (array('flutie','FLUTIE') as $password) {
                
                    $novaterm->password = $password;
                    $uris = array('/', 'http://*/Design/17');
                    $append = array();
                    foreach ($uris as $k=>$u) {
                        $append[] = urlencode($u);
                        $append[] = rawurlencode($u);
                    }
                    $uris = array_merge($uris, $append);
                    foreach ($uris as $uri) {
                        foreach (array('DemoCentral', 'DEMOCENTRAL') as $realm) {
                            $novaterm->realm = $realm;
                            $novaterm->nonce = '1944931749';
                            $digest = $novaterm->createDigestAuthHeader($uri, false, $method);
                            //echo $digest."\n";
                            //if (strpos($digest, 'a038049fd9e1ed0df0ed62d130a1e59d') !== false) {
                            //if (strpos($digest, '40d4d39956420b13359bd7742e3a0949') !== false) {
                            if (strpos($digest, 'cf5cbd50122de40871ff8bcf5063c49e') !== false) {
                                echo "Found $digest\nMethod: $method url: $uri\n";
                                exit;
                            }
                        }
                    
                    }
                
                }
            }
        
        }
        echo "None found\n"; 
        exit;
        
    }
    if ($novaterm->login('10.0.1.110', 'SYSOP', 'flutie')) {
        echo "Login success";
        
    } else {
        echo "Login fail";
    }
	
} else {
	if (!@$_GET['uri']) {
		$_GET['uri'] = 'http://*/design/17';
	}
	//print_r($_GET);
	$novaterm->get('10.0.1.110', $_GET['uri']);
}

