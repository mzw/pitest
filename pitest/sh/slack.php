<?php

if( $argc != 3 ){
        echo "Invalid usage: give \${username} and \${text}\n";
        exit(0);
}

$username = $argv[1];
$text = $argv[2];

$webhook_url = 'https://hooks.slack.com/services/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';

$msg = array(
    'username' => $username, 
    'text' => $text
);
$msg = json_encode($msg);
$msg = 'payload=' . urlencode($msg);

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $webhook_url);
curl_setopt($ch, CURLOPT_HEADER, false);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, $msg);
curl_exec($ch);
curl_close($ch);

?>

