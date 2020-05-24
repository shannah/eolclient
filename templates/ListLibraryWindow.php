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
<div id="toolbar">
    <button>Release</button>
    <button>Delete</button>
    <button>Upload</button>
    
</div>

<?php foreach ($xpath->query('//ul') as $ul): 
    foreach ($xpath->query('li', $ul) as $li): ?>
       
    
    <?php endforeach; 
endforeach; ?>
</body>
</html>