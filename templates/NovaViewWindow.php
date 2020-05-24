<!doctype html>
<html>
<head>
  <link href="assets/fontawesome/css/all.css" rel="stylesheet">
  <style>
  .icon {
    display:block;
    float:left;
    text-align:center;
    margin: 10px;
    text-decoration: none;
  }
  
  .icon i {
    display:block;
    text-align:center;
    font-size: 36px;
    color: gray;
    margin-bottom: 5px;
  }
  
  .icon span {
    display:block;
    text-align:center;
    font-family: Helvetica, sans-serif;
    color: #333;

  }
  </style>
</head>
<body>
<?php foreach ($xpath->query('//title') as $title):?>
    <h1><?php echo htmlspecialchars($title->textContent); ?></h1>
<?php endforeach; ?>
<?php foreach ($xpath->query('//a') as $a): 
    $image = null;
    foreach ($xpath->query('image', $a) as $im) {
        $image = $im;
    }
    $src = $image->getAttribute('src');
    $src = basename($src);
    $class = '';
    //echo $src;
    if ($src == 'NV Library Icon') {
        $class .= 'fa-database';
    } else if ($src == 'NV Conference') {
        $class .= ' fa-comments';
    } else if ($src == 'NV ListUsers') {
        $class .= ' fa-sort-alpha-down';
    } else if ($src == 'nv forum') {
        $class .= ' fa-newspaper';
    } else if ($src == 'NV Newscan') {
        $class .= ' fa-rss';
    } else if ($src == 'NV SelectNewscan') {
        $class .= ' fa-rss';
    } else if ($src == 'NV Vote') {
        $class .= ' fa-poll';
    } else if ($src == 'NV Edit Password') {
        $class .= ' fa-key';
    } else if ($src == "NV Who's Online") {
        $class .= ' fa-user-friends';
    } else if ($src == 'NV RunScript') {
        $class .= ' fa-external-link-alt';
    }
    
?>
		  			
		  			
    <a class="icon" href="<?php echo $_SERVER['PHP_SELF'].'?uri='.$a->getAttribute('href'); ?>" title="<?php echo htmlspecialchars($image->textContent); ?>">
    <i class="fas <?php echo $class;?>"></i>
    <span><?php echo htmlspecialchars($image->textContent); ?></span>
    </a>
<?php endforeach; ?>
</body>
</html>