$files = Get-ChildItem server\app\modules\models *.py -rec
foreach ($file in $files)
{
    (Get-Content $file.PSPath) |
    Foreach-Object { $_ -replace "^(import .+_pb2)", 'from . $1' } |
    Set-Content $file.PSPath
}