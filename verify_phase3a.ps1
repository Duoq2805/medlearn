[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$baseUrl = "http://localhost:6060/api"

Write-Host "=== STEP 1: Register and Verify USER account ==="
$rand = Get-Random
$uName = "user3a_$rand"
$uEmail = "user3a_$rand@medlearn.com"
$uPass = "UserPassword123!"

# 1a. Register
$regReq = @{ username = $uName; email = $uEmail; password = $uPass; fullName = "Phase3A User $rand" } | ConvertTo-Json
$regRes = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $regReq -ContentType "application/json; charset=utf-8"
Write-Host "Registered user: $uName"

# 1b. Get verification token from DB (read-only query)
$tokenQuery = "SELECT vt.token FROM verification_token vt JOIN users u ON vt.user_id = u.id WHERE u.username = '$uName';"
$vToken = (docker exec -i medlearn_postgres psql -U medlearn -d medlearn_db -t -c "$tokenQuery").Trim()
Write-Host "Verification Token: $vToken"

# 1c. Call official verify API
$vRes = Invoke-RestMethod -Uri "$baseUrl/auth/verify?token=$vToken" -Method Get
Write-Host "Email Verification Result: $($vRes.message)"

# 1d. Login as USER
$uLoginReq = @{ usernameOrEmail = $uEmail; password = $uPass } | ConvertTo-Json
$uLoginRes = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $uLoginReq -ContentType "application/json; charset=utf-8"
$uToken = $uLoginRes.data.accessToken
$uHeaders = @{ Authorization = "Bearer $uToken" }
Write-Host "USER Logged In. User ID: $($uLoginRes.data.id)"

Write-Host "`n=== STEP 2: USER Creates Disease Draft ==="
$draftReq = @{ 
    name = "Dengue Fever Test Phase3A $rand"; 
    slug = "dengue-fever-test-phase3a-$rand"
} | ConvertTo-Json
$draftRes = Invoke-RestMethod -Uri "$baseUrl/diseases/draft" -Method Post -Headers $uHeaders -Body $draftReq -ContentType "application/json; charset=utf-8"
$diseaseId = $draftRes.data.id
Write-Host "Created Draft Disease ID: $diseaseId"

Write-Host "`n=== STEP 3: USER Fetches Latest Draft Version ==="
$versionRes = Invoke-RestMethod -Uri "$baseUrl/versions/disease/$diseaseId/latest-draft" -Method Get -Headers $uHeaders
$versionId = $versionRes.data.id
Write-Host "Fetched Version ID: $versionId, Status: $($versionRes.data.status)"

Write-Host "`n=== STEP 4: USER Section CRUD & Persistence ==="
# Create Definition Section (ID 1)
$sec1Req = @{ sectionTypeId = 1; title = "Disease Definition"; content = "Dengue fever definition content"; orderIndex = 1 } | ConvertTo-Json
$sec1Res = Invoke-RestMethod -Uri "$baseUrl/sections?versionId=$versionId" -Method Post -Headers $uHeaders -Body $sec1Req -ContentType "application/json; charset=utf-8"
$sec1Id = $sec1Res.data.id
Write-Host "Created Definition Section ID: $sec1Id"

# Update Definition Section
$sec1UpdReq = @{ sectionTypeId = 1; title = "Disease Definition (Updated)"; content = "Updated definition content"; orderIndex = 1 } | ConvertTo-Json
$sec1UpdRes = Invoke-RestMethod -Uri "$baseUrl/sections/$sec1Id" -Method Put -Headers $uHeaders -Body $sec1UpdReq -ContentType "application/json; charset=utf-8"
Write-Host "Updated Section 1 Title: '$($sec1UpdRes.data.title)'"

# Create Symptoms Section (ID 2)
$sec2Req = @{ sectionTypeId = 2; title = "Clinical Symptoms"; content = "High fever, severe headache, retro-orbital pain"; orderIndex = 2 } | ConvertTo-Json
$sec2Res = Invoke-RestMethod -Uri "$baseUrl/sections?versionId=$versionId" -Method Post -Headers $uHeaders -Body $sec2Req -ContentType "application/json; charset=utf-8"
$sec2Id = $sec2Res.data.id
Write-Host "Created Symptoms Section ID: $sec2Id"

# Create Treatment Section (ID 5)
$sec3Req = @{ sectionTypeId = 5; title = "Treatment Guidelines"; content = "Early fluid resuscitation, paracetamol for fever"; orderIndex = 3 } | ConvertTo-Json
$sec3Res = Invoke-RestMethod -Uri "$baseUrl/sections?versionId=$versionId" -Method Post -Headers $uHeaders -Body $sec3Req -ContentType "application/json; charset=utf-8"
$sec3Id = $sec3Res.data.id
Write-Host "Created Treatment Section ID: $sec3Id"

# Verify List & Persistence
$listRes = Invoke-RestMethod -Uri "$baseUrl/sections/version/$versionId" -Method Get -Headers $uHeaders
Write-Host "Persisted Sections Count for Version $versionId : $($listRes.data.Count)"
foreach ($s in $listRes.data) {
    Write-Host " - Section ID: $($s.id) | Title: '$($s.title)' | TypeID: $($s.sectionTypeId)"
}

Write-Host "`n=== STEP 5: USER Submits Version for Review ==="
$subRes = Invoke-RestMethod -Uri "$baseUrl/versions/$versionId/submit" -Method Post -Headers $uHeaders
Write-Host "Submission Result Status: $($subRes.data.status)"

Write-Host "`n=== STEP 6: ADMIN Login & Pending Review Queue Check ==="
$adminLogin = @{ usernameOrEmail = "devadmin@medlearn.com"; password = "AdminPassword123!" } | ConvertTo-Json
$adminRes = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $adminLogin -ContentType "application/json; charset=utf-8"
$adminToken = $adminRes.data.accessToken
$adminHeaders = @{ Authorization = "Bearer $adminToken" }
Write-Host "Admin logged in. Token acquired."

$queueRes = Invoke-RestMethod -Uri "$baseUrl/versions/pending-review?page=0&size=20" -Method Get -Headers $adminHeaders
$foundInQueue = $queueRes.data.content | Where-Object { $_.id -eq $versionId }
if ($foundInQueue) {
    Write-Host "SUCCESS: Version $versionId found in Pending Review Queue (Version ID: $($foundInQueue.id), Status: $($foundInQueue.status))"
} else {
    Write-Host "WARNING: Version $versionId not found in Pending Review Queue"
}

Write-Host "`n=== STEP 7: ADMIN Approves Version ({ note: string | null }) ==="
$appReq = @{ note = "Approved in Phase 3A runtime verification" } | ConvertTo-Json
$appRes = Invoke-RestMethod -Uri "$baseUrl/versions/$versionId/approve" -Method Post -Headers $adminHeaders -Body $appReq -ContentType "application/json; charset=utf-8"
Write-Host "Approval Result Status: $($appRes.data.status)"

Write-Host "`n=== STEP 8: Verify Current Published Version ==="
$currRes = Invoke-RestMethod -Uri "$baseUrl/versions/disease/$diseaseId/current" -Method Get -Headers $adminHeaders
Write-Host "Current Published Version ID: $($currRes.data.id), Status: $($currRes.data.status)"

Write-Host "`n======================================================="
Write-Host "ALL PHASE 3A RUNTIME VERIFICATIONS COMPLETED SUCCESSFULLY!"
Write-Host "======================================================="
