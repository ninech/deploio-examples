<?php

use Imagine\Image\ImageInterface;

require __DIR__.'/../vendor/autoload.php';

const MIN_SIZE = 20;
const MAX_SIZE = 2000;

$options = [
    'options' => [
        'default' => 200,
        'min_range' => MIN_SIZE,
        'max_range' => MAX_SIZE,
    ],
];
$size = filter_input(INPUT_GET, 'size', FILTER_VALIDATE_INT, $options);

$imagine = new Imagine\Gd\Imagine();

$box = new Imagine\Image\Box($size, $size);

$image = $imagine->open(__DIR__.'/../resources/deploio.png')
    ->thumbnail($box, ImageInterface::THUMBNAIL_OUTBOUND)
    ->get('png')
;

?>
<html lang="en">
<head>
    <title>deplo.io plain PHP demo</title>
    <link rel="icon" href="/favicon.svg" type="image/svg+xml">
    <style>
        body {
            margin: 0 auto;
            padding-top: 60px;
            max-width: 800px;
        }
    </style>
</head>
<body>
<h1>Plain PHP Demo is working!</h1>
<p>
    This simplistic PHP application demonstrates how to install a PHP application on deplo.io.
    It uses the <a href="https://github.com/php-imagine/Imagine" target="_blank"><code>imagine/imagine</code></a> library with the GD PHP extension <code>ext-gd</code> to scale an image to a different size.
    deplo.io uses the <code>composer.json</code> to know that it needs to provide <code>ext-gd</code> and then uses composer to install the dependencies.
</p>
<img title="deplo.io logo scaled in PHP" alt="deplo.io" src="data:image/png;base64,<?= base64_encode($image) ?>" />
<p>
    This image is dynamically scaled with Imagine.
</p>
<form method="get">
    <label for="size">Image size (20 - 2000): </label>
    <input type="number" id="size" name="size" min="<?= MIN_SIZE ?>" max="<?= MAX_SIZE ?>" value="<?= $size ?>" /> px<br/>
    <input type="submit" value="Scale Image"/>
</form>
<h1>MySQL Test</h1>
<?php

$dbname = getenv('DBNAME');
$dbuser = getenv('DBUSER');
$dbpass = getenv('DBPASS');
$dbhost = getenv('DBHOST');

$link = mysqli_init();
mysqli_options($link, MYSQLI_CLIENT_SSL_VERIFY_SERVER_CERT, false);
mysqli_ssl_set($link, NULL, NULL, '', NULL, NULL);
mysqli_real_connect($link, $dbhost, $dbuser, $dbpass, NULL, NULL, NULL, MYSQLI_CLIENT_SSL | MYSQLI_CLIENT_SSL_DONT_VERIFY_SERVER_CERT );

if (!$link) {
  echo mysqli_connect_errno() . ":" . mysqli_connect_error();
  exit;
}

mysqli_select_db($link, $dbname) or die("Could not open the db '$dbname'");

$test_query = "SHOW TABLES FROM $dbname";
$result = mysqli_query($link, $test_query);

$tblCnt = 0;
while($tbl = mysqli_fetch_array($result)) {
    $tblCnt++;
    #echo $tbl[0]."<br />\n";
}

if (!$tblCnt) {
    echo "There are no tables<br />\n";
} else {
    echo "There are $tblCnt tables<br />\n";
}

?>

</body>
</html>
