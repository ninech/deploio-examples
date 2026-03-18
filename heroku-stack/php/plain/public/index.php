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
</body>
</html>
