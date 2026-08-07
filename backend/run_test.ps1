Get-Content '..\.env' | ForEach-Object { 
  $line = $_.Trim(); 
  if ($line -and -not $line.StartsWith('#')) { 
    $k, $v = $line.Split('=', 2); 
    [Environment]::SetEnvironmentVariable($k.Trim(), $v.Trim(), 'Process') 
  } 
}
.\mvnw.cmd test
