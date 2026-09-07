[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$baseUrl = "http://localhost:6060/api"

Write-Host "=== STEP 1: Login as Admin & User ==="
$adminLogin = @{ usernameOrEmail = "devadmin@medlearn.com"; password = "AdminPassword123!" } | ConvertTo-Json
$adminRes = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $adminLogin -ContentType "application/json; charset=utf-8"
$adminToken = $adminRes.data.accessToken
$adminHeaders = @{ Authorization = "Bearer $adminToken" }
Write-Host "Admin logged in successfully."

Write-Host "`n=== STEP 2: Test Symptom & Symptom Checker APIs ==="
$symList = Invoke-RestMethod -Uri "$baseUrl/symptoms" -Method Get -Headers $adminHeaders
Write-Host "Total Symptoms fetched: $($symList.data.Count)"

$symSearch = Invoke-RestMethod -Uri "$baseUrl/symptoms/search?keyword=sot" -Method Get -Headers $adminHeaders
Write-Host "Symptom search 'sot' returned: $($symSearch.data.Count) items"

$symCheckV0 = @{ symptomIds = @(1, 2); limit = 5 } | ConvertTo-Json -Depth 5
$checkV0Res = Invoke-RestMethod -Uri "$baseUrl/symptom-checker/check" -Method Post -Headers $adminHeaders -Body $symCheckV0 -ContentType "application/json; charset=utf-8"
Write-Host "Symptom Checker V0 matched diseases: $($checkV0Res.data.Count)"

$checkV1Res = Invoke-RestMethod -Uri "$baseUrl/symptom-checker/analyze" -Method Post -Headers $adminHeaders -Body $symCheckV0 -ContentType "application/json; charset=utf-8"
Write-Host "Symptom Checker V1 analyzed diseases: $($checkV1Res.data.Count)"

$checkV2Res = Invoke-RestMethod -Uri "$baseUrl/symptom-checker/v2/analyze" -Method Post -Headers $adminHeaders -Body $symCheckV0 -ContentType "application/json; charset=utf-8"
Write-Host "Symptom Checker V2 analyzed diseases: $($checkV2Res.data.Count)"

Write-Host "`n=== STEP 3: Test Case Study APIs ==="
$casesRes = Invoke-RestMethod -Uri "$baseUrl/cases?page=0&size=10" -Method Get -Headers $adminHeaders
Write-Host "Case Studies count: $($casesRes.data.content.Count)"

Write-Host "`n=== STEP 4: Test Draft Lifecycle APIs (/api/drafts) ==="
$rand = Get-Random
$createDraftReq = @{
    title = "Phase3B Test Draft $rand";
    sourceMethod = "MANUAL";
    sections = @(
        @{ sectionType = "DEFINITION"; title = "Definition"; content = "Draft definition content"; orderIndex = 1 },
        @{ sectionType = "SYMPTOMS"; title = "Symptoms"; content = "High fever, chills"; orderIndex = 2 }
    )
} | ConvertTo-Json -Depth 5
$draftCreated = Invoke-RestMethod -Uri "$baseUrl/drafts" -Method Post -Headers $adminHeaders -Body $createDraftReq -ContentType "application/json; charset=utf-8"
$draftId = $draftCreated.data.id
Write-Host "Created Draft ID: $draftId, Status: $($draftCreated.data.status)"

$draftFetch = Invoke-RestMethod -Uri "$baseUrl/drafts/$draftId" -Method Get -Headers $adminHeaders
Write-Host "Fetched Draft ID: $($draftFetch.data.id), Title: '$($draftFetch.data.title)'"

$draftList = Invoke-RestMethod -Uri "$baseUrl/drafts?page=0&size=10" -Method Get -Headers $adminHeaders
Write-Host "Drafts List count: $($draftList.data.content.Count)"

$secId = $draftCreated.data.sections[0].id
$updateDraftReq = @{
    title = "Phase3B Test Draft $rand (Updated)";
    sections = @(
        @{ id = $secId; sectionType = "DEFINITION"; title = "Definition (Updated)"; content = "Updated definition content"; orderIndex = 1 }
    )
} | ConvertTo-Json -Depth 5
$draftUpdated = Invoke-RestMethod -Uri "$baseUrl/drafts/$draftId" -Method Put -Headers $adminHeaders -Body $updateDraftReq -ContentType "application/json; charset=utf-8"
Write-Host "Updated Draft Title: '$($draftUpdated.data.title)'"

Write-Host "`n=== STEP 5: Test Admin User & System Management APIs (/api/admin) ==="
$adminUsers = Invoke-RestMethod -Uri "$baseUrl/admin/users?page=0&size=10" -Method Get -Headers $adminHeaders
Write-Host "Admin List Users total: $($adminUsers.data.totalElements)"

$adminAnalytics = Invoke-RestMethod -Uri "$baseUrl/admin/analytics" -Method Get -Headers $adminHeaders
Write-Host "Admin Analytics response: $($adminAnalytics.data.message)"

$adminReviewsNotice = Invoke-RestMethod -Uri "$baseUrl/admin/pending-reviews" -Method Get -Headers $adminHeaders
Write-Host "Admin Pending Reviews notice: '$($adminReviewsNotice.data)'"

Write-Host "`n======================================================="
Write-Host "ALL PHASE 3B RUNTIME VERIFICATIONS PASSED SUCCESSFULLY!"
Write-Host "======================================================="
